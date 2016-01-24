package com.soledede.recomend.rest

import java.io.{FileOutputStream, OutputStream, InputStream, ByteArrayInputStream}
import java.util.regex.Pattern

import akka.actor.{Actor}
import akka.event.slf4j.SLF4JLogging
import akka.util.ByteString
import com.alibaba.fastjson.util.Base64
import com.soledede.recomend.config.Configuration
import com.soledede.recomend.dao.HbaseRecommendModel
import com.soledede.recomend.domain.Failure
import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date}
import com.soledede.recomend.entity.{RecommendResult, Msg}
import com.soledede.recomend.file.FileProcessService
import com.soledede.recomend.imageSearch.ImageSearchService
import com.soledede.recomend.ui.RecommendUI
import net.liftweb.json.Serialization._
import net.liftweb.json.{DefaultFormats, DateFormat, Formats}
import redis.ByteStringFormatter
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.MetaMarshallers
import spray.json.DefaultJsonProtocol._
import scala.Some
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._
import spray.http.MediaTypes._
import spray.http.BodyPart
import net.liftweb.json._
import net.liftweb.json.Serialization.{write, read}


/*object MyS extends DefaultJsonProtocol {
  implicit val rmsgFormat = jsonFormat2(RMsg)
}*/

case class RMsg(result: Seq[String], code: Int)

object RMsg {
  implicit val rmsgFormat = jsonFormat2(RMsg.apply)
}

case class VMsg(result: Seq[Video])

object Video {
  implicit val video = jsonFormat2(Video.apply)



}

case class Video(name: String, url: String)

object VMsg {
  implicit val videoMsg = jsonFormat1(VMsg.apply)

}

case class Base64Post(start: String, size: String, filename: String, filedata: String)

object Base64Post {
  implicit val base64Post = jsonFormat4(Base64Post.apply)
}


/**
  *
  * REST Service actor.
  */
class RestRecommendServiceActor extends Actor with RestService {

  implicit def actorRefFactory = context

  //选择推荐还是其他,如图像检索
  def receive = if (openRecommend) runRoute(rest) else runRoute(restRout)
}

/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging with Configuration with spray.httpx.SprayJsonSupport {


  //import RJsonProtocol._


  var resModuleService: HbaseRecommendModel = null
  var defaultRecommendUI: RecommendUI = null

  var fileProcessService: FileProcessService = null

  var imageSearchService: ImageSearchService = null

  if (openRecommend) {
    resModuleService = HbaseRecommendModel()
    defaultRecommendUI = RecommendUI()
  } else {
    //image search
    fileProcessService = FileProcessService()
    imageSearchService = ImageSearchService()
  }


  // we use the enclosing ActorContext's or ActorSystem's dispatcher for our Futures and Scheduler
  implicit val executionContext = actorRefFactory.dispatcher

  implicit val liftJsonFormats = new Formats {
    val dateFormat = new DateFormat {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")

      def parse(s: String): Option[Date] = try {
        Some(sdf.parse(s))
      } catch {
        case e: Exception => None
      }

      def format(d: Date): String = sdf.format(d)
    }
  }

  implicit val string2Date = new FromStringDeserializer[Date] {
    def apply(value: String) = {
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      try Right(sdf.parse(value))
      catch {
        case e: ParseException => {
          Left(MalformedContent("'%s' is not a valid Date value" format (value), e))
        }
      }
    }
  }

  implicit val recommendRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse {
      response =>
        response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`),
          write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  val rest = respondWithMediaType(MediaTypes.`application/json`) {
    //"res" / Segment 匹配字符串
    //数字 LongNumber
    path("recommend" / "catagory" / Segment) {
      keyword =>
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              val catagory = defaultRecommendUI.recommendMostLikeCatagoryIdByKeywords(keyword)
              if (catagory == null) Right(Msg("未能匹配到合适类目!", -1))
              else Right(catagory)
            }
        }
    } ~
      path("recommend" / Segment / IntNumber) {
        (userId, number) =>
          get {
            ctx: RequestContext =>
              handleRequest(ctx) {
                if (number == null || number <= 0 || number > 80)
                  Right(Msg("推荐数量必须大于0,并且不能超过80个", -1))
                else Right(defaultRecommendUI.recommendByUserId(userId, number))
              }
          }
      }
  }


  val videoService = imageHost + ":" + imagePort

  val recommend_introduce = "http://" + videoService + "/video/recommend_introduce.mp4";
  val recommend_indtroduceName = "推荐系统介绍";

  val leastSquare = "http://" + videoService + "/video/ml-least_square.mp4";
  val leastSquareName = "最小二乘法";
  val boltzmannCf = "http://" + videoService + "/video/boltzmann_cf.mp4";
  val boltzmannCfName = "玻尔兹曼机协同过滤";
  val native_bayes = "http://" + videoService + "/video/native_bayes.mp4";
  val native_bayesName = "朴素贝叶斯实现分类";

  val em = "http://" + videoService + "/video/em.mp4";
  val emName = "EM算法";
  val hmm_viterbi_decode = "http://" + videoService + "/video/hmm_viterbi_decode.mp4";
  val hmm_viterbi_decodeName = "Viterbi解码算法";

  val videoList = VMsg(List(Video(recommend_indtroduceName, recommend_introduce)))

  //implicit def json4sFormats: Formats = DefaultFormats


  val restRout: Route = {
    path("") {
      get {
        /* respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
           complete(index)
         }*/
        redirect("http://soledede.com/", StatusCodes.MovedPermanently)
      }
    } ~
      path("soledede" / "video") {
        detach() {
          get {
            respondWithMediaType(MediaTypes.`application/json`) {
              complete {
                videoList
              }
            }
          }
        }
      } ~
      path("soledede" / "image") {
        detach() {
          post {
            entity(as[MultipartFormData]) { data =>
              //println(data)
              complete {
                try {
                  val start = data.get("start").get.entity.data.toByteString.decodeString("utf-8").toInt
                  val size = data.get("size").get.entity.data.toByteString.decodeString("utf-8").toInt
                  data.get("image") match {
                    case Some(imageEntity) =>
                      //println(imageEntity)
                      val imageData = imageEntity.entity.data.toByteArray
                      //val contentType = imageEntity.headers.find(h => h.is("content-type")).get.value
                      val fileName = imageEntity.headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
                      //println(s"Uploaded $fileName")
                      val result = fileProcessService.saveAttachment(fileName.trim, imageData, fileDir)
                      val resultList = imageSearchService.search(fileName.trim, start, size)
                      log.debug("+++++++++++++++++++++++++++" + resultList)
                      if (resultList == null || resultList.size == 0)
                        RMsg(List(), -1)
                      else RMsg(resultList, if (result) 0 else -1)
                    case None =>
                      println("No files")
                      "Not OK"
                      RMsg(List(), -1)
                  }
                } catch {
                  case e: Exception =>
                    log.error("faield!", e)
                    RMsg(List(), -1)
                }
              }
            }

          }
        }
      } ~
      path("soledede" / "imagejson") {
        detach() {
          post {
            respondWithMediaType(MediaTypes.`application/json`) {
              entity(as[Base64Post]) { postJsonData =>
                complete {

                  try {
                    val start = postJsonData.start.trim.toInt

                    val size = postJsonData.size.trim.toInt

                    val fileName = postJsonData.filename.trim

                    // String imageDataBytes = postJsonData.substring(postJsonData.indexOf(",") + 1);
                    //new ByteArrayInputStream(Base64.decode(postJsonData.filedata.getBytes(), Base64.DEFAULT));

                    val imageDataStream = new ByteArrayInputStream(Base64.decodeFast(postJsonData.filedata))
                    val result = fileProcessService.saveAttachment(fileName, imageDataStream, fileDir)
                    val resultList = imageSearchService.search(fileName, start, size)
                    log.debug("+++++++++++++++++++++++++++" + resultList)
                    if (resultList == null || resultList.size == 0)
                      RMsg(List(), -1)
                    else RMsg(resultList, if (result) 0 else -1)
                  } catch {
                    case e: Exception =>
                      log.error("faield!", e)
                      RMsg(List(), -1)
                  }
                }
              }

            }
          }
        }
      }
  }

  lazy val index =
    <html>
      <body>
        <h1>欢迎...</h1>
      </body>
    </html>


  /**
    * Handles an incoming request and create valid response for it.
    *
    * @param ctx         request context
    * @param successCode HTTP Status code for success
    * @param action      action to perform
    */
  protected def handleRequest(ctx: RequestContext, successCode: StatusCode = StatusCodes.OK)(action: => Either[Failure, _]) {
    action match {
      case Right(result: Object) =>
        ctx.complete(successCode, write(result))
      case Left(error: Failure) =>
        ctx.complete(error.getStatusCode, net.liftweb.json.Serialization.write(Map("error" -> error.message)))
      case _ =>
        ctx.complete(StatusCodes.InternalServerError)
    }
  }
}

/*
path("file") {
detach() {
post {
respondWithMediaType(`application/json`) {
entity(as[MultipartFormData]) { formData =>
complete {
val details = formData.fields.map {
case (name, BodyPart(entity, headers)) =>
//val content = entity.buffer
val content = new ByteArrayInputStream(entity.data.toByteArray)
val contentType = headers.find(h => h.is("content-type")).get.value
val fileName = headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
val result = saveAttachment(fileName, content)
(contentType, fileName, result)
case _ =>
}
s"""{"status": "Processed POST request, details=$details" }"""
}
}
}
}
}
}*/


/*
post {
handleWith { data: MultipartFormData =>
data.get("files[]") match {
case Some(imageEntity) =>
val size = imageEntity.entity.data.length
println(s"Uploaded $size")
case None =>
println("No files")
}
}
}*/

