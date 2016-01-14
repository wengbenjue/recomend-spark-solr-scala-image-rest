package com.soledede.recomend.rest

import java.io.{FileOutputStream, OutputStream, InputStream, ByteArrayInputStream}

import akka.actor.{Actor}
import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.config.Configuration
import com.soledede.recomend.dao.HbaseRecommendModel
import com.soledede.recomend.domain.Failure
import java.text.{ParseException, SimpleDateFormat}
import java.util.Date
import com.soledede.recomend.entity.Msg
import com.soledede.recomend.ui.RecommendUI
import net.liftweb.json.Serialization._
import net.liftweb.json.{DefaultFormats, DateFormat, Formats}
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.MetaMarshallers
import spray.json.DefaultJsonProtocol
import scala.Some
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._
import spray.http.MediaTypes._
import spray.http.BodyPart

case class RMsg(code: Boolean)

object RJsonProtocol extends DefaultJsonProtocol {
  implicit val PersonFormat = jsonFormat1(RMsg)
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

  final val fileDir = "/Users/soledede/Documents/"

  import RJsonProtocol._


  var resModuleService: HbaseRecommendModel = null
  var defaultRecommendUI: RecommendUI = null

  if (openRecommend) {
    resModuleService = HbaseRecommendModel()
    defaultRecommendUI = RecommendUI()
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
      path("soledede" / "image") {
        detach() {
          post {
            entity(as[MultipartFormData]) { data =>
              println(data)
              complete {
                data.get("image") match {
                  case Some(imageEntity) =>
                    println(imageEntity)
                    val imageData = imageEntity.entity.data.toByteArray
                    //val contentType = imageEntity.headers.find(h => h.is("content-type")).get.value
                    val fileName = imageEntity.headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
                    println(s"Uploaded $fileName")
                    val result = saveAttachment(fileName, imageData)
                    RMsg(result)
                  case None =>
                    println("No files")
                    "Not OK"
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


  private def saveAttachment(fileName: String, content: Array[Byte]): Boolean = {
    saveAttachment[Array[Byte]](fileName, content, { (is, os) => os.write(is) })
    true
  }

  private def saveAttachment(fileName: String, content: InputStream): Boolean = {
    saveAttachment[InputStream](fileName, content, { (is, os) =>
      val buffer = new Array[Byte](16384)
      Iterator
        .continually(is.read(buffer))
        .takeWhile(-1 !=)
        .foreach(read => os.write(buffer, 0, read))
    }
    )
  }

  private def saveAttachment[T](fileName: String, content: T, writeFile: (T, OutputStream) => Unit): Boolean = {
    try {
      val fos = new java.io.FileOutputStream(fileDir + fileName)
      writeFile(content, fos)
      fos.close()
      true
    } catch {
      case _ => false
    }
  }


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

