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
import com.soledede.recomend.entity.{MergeCloud, RecommendResult, Msg}
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

object Video extends Configuration{
  implicit val video = jsonFormat4(Video.apply)

  def listStaticVideo():Seq[Video] = {

    val videoService = imageHost + ":" + imagePort


    val leastSquareName = "形象理解最小二乘法"
    val leastSquareDesc = "拟合最优超平面，使得误差和最小"
    val leastSquareUrl = "http://" + videoService + "/video/ml-least_square.mp4"
    val leastImg = "http://a1.qpic.cn/psb?/V10sqCyZ06hzDJ/lWpCMe4e0G5ltPhJhlaO0tCS9oC5m3XzqzDWvw5MSGY!/b/dAIBAAAAAAAA&bo=IAPSAQAAAAADB9I!&rf=viewer_4"
    val leastSquareVideo = Video(leastSquareName,leastSquareDesc,leastSquareUrl,leastImg)


    val pictureGradientName = "形象理解梯度下降算法"
    val pictureGradienDesc = "既不能得全局最小值,也得逼近局部最小值"
    val pictureGradienUrl = "http://" + videoService + "/video/picture_gradient.mp4"
    val pictureGradienImg = "http://t.cn/R4GsikL"
    val pictureGradienVideo = Video(pictureGradientName,pictureGradienDesc,pictureGradienUrl,pictureGradienImg)



    val boltzmannCfName = "玻尔兹曼机协同过滤"
    val boltzmannCfDesc = "怎么用深度学习建模评分矩阵"
    val boltzmannCfUrl = "http://" + videoService + "/video/boltzmann_cf.mp4"
    val boltzmannCfImg = "http://a2.qpic.cn/psb?/V10sqCyZ06hzDJ/AGSUYdrRIAd*2p3K69CMCXcDymO41Gb6IY0gWFtWOLM!/b/dP0AAAAAAAAA&bo=IAP6AQAAAAADB*o!&rf=viewer_4"
    val boltzmannCfVideo = Video(boltzmannCfName,boltzmannCfDesc,boltzmannCfUrl,boltzmannCfImg)



    val hmmViterbiDecodeName = "Viterbi解码算法"
    val hmmViterbiDecodeDesc = "信道模型之解码问题"
    val hmmViterbiDecodeUrl = "http://" + videoService + "/video/hmm_viterbi_decode.mp4"
    val hmmViterbiDecodeImg = "http://a3.qpic.cn/psb?/V10sqCyZ06hzDJ/YytiurFA2VyacA9Gi.2xLPWBFaTUMYBHfl51.subhNg!/b/dPsAAAAAAAAA&bo=IAPMAQAAAAADB8w!&rf=viewer_4"
   // val hmmViterbiDecodeVideo = Video(hmmViterbiDecodeName,hmmViterbiDecodeDesc,hmmViterbiDecodeUrl,hmmViterbiDecodeImg)


    val hmmBaumWelchName = "HMM-前向后向算法"
    val hmmBaumWelchDesc = "利用baumwelch求隐变量,利用Dynamic Programming可以大大减少计算量"
    val hmmBaumWelchUrl = "http://" + videoService + "/video/hmm-baum-welch.mp4"
    val hmmBaumWelchImg = "http://a2.qpic.cn/psb?/V10sqCyZ06hzDJ/c3hb4a0v4qAAxeRbYF69E7CpMEaPV7*sPBEJyIfydPE!/b/dKYAAAAAAAAA&bo=IAO.AQAAAAADB74!&rf=viewer_4"
    val hmmBaumWelchVideo = Video(hmmBaumWelchName,hmmBaumWelchDesc,hmmBaumWelchUrl,hmmBaumWelchImg)



    val nativeBayesName = "朴素贝叶斯实现文本分类"
    val nativeBayesDesc = "Bayes实现垃圾邮件过滤,智能聊天系统,主题分类..."
    val nativeBayesUrl = "http://" + videoService + "/video/native_bayes.mp4"
    val nativeBayesimg = "http://a1.qpic.cn/psb?/V10sqCyZ06hzDJ/vnBmrXm2f8OYoRIpXsGEzRgkEndLotRl8qioRj7Tvqs!/b/dAIBAAAAAAAA&bo=IAPEAQAAAAADB8Q!&rf=viewer_4"
    val nativeBayesVideo = Video(nativeBayesName,nativeBayesDesc,nativeBayesUrl,nativeBayesimg)



    val emName = "期望最大化(EM)算法"
    val emDesc = "如果将样本看作观察值，潜在类别看作是隐藏变量那么聚类问题也就是参数估计问题"
    val emUrl = "http://" + videoService + "/video/em.mp4"
    val emImg = "http://a1.qpic.cn/psb?/V10sqCyZ06hzDJ/bv8tPwrLygsnQ5f8SF1T9V5fy4wNItGw.RsF0Arp3Ik!/b/dPwAAAAAAAAA&bo=IAO5AQAAAAADB7k!&rf=viewer_4"
    val emVideo = Video(emName,emDesc,emUrl,emImg)


    val solrItemcfName = "Solr实现ItemCF个性化推荐算法"
    val solrItemcfDesc = "借助lucene的向量空间模型以及solr的facet实现推荐"
    val solrItemcfUrl = "http://" + videoService + "/video/solr_itemcf.mp4"
    val solrItemcfImg = "http://t.cn/R4JasVf"
    val solrItemcfVideo = Video(solrItemcfName,solrItemcfDesc,solrItemcfUrl,solrItemcfImg)


    val recommendIndtroduceName = "推荐系统介绍"
    val recommendIntroduceDesc = "一、推荐系统的介绍,二、大数据介绍及相关环境搭建"
    val recommendIntroduceUrl = "http://" + videoService + "/video/recommend_introduce.mp4"
    val recommendIntroduceImg = "http://a2.qpic.cn/psb?/V10sqCyZ06hzDJ/yfsAI2wh5B4M50pXAg6m5gHEqA8lkkmze1yf9LnzUSw!/b/dKkAAAAAAAAA&bo=IAPFAQAAAAADB8U!&rf=viewer_4"
    val recomndeIntroduceVideo = Video(recommendIndtroduceName,recommendIntroduceDesc,recommendIntroduceUrl,recommendIntroduceImg)


    List(leastSquareVideo,pictureGradienVideo,boltzmannCfVideo,hmmBaumWelchVideo,nativeBayesVideo,emVideo,solrItemcfVideo,recomndeIntroduceVideo)
  }

}

case class Video(name: String,descrition:String, url: String,img: String)

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
      }~
    path("mergecloud"){
      get{
        ctx: RequestContext =>
          handleRequest(ctx) {
              Right(new MergeCloud())
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
      path("soledede" / "video") {
        detach() {
          get {
            respondWithMediaType(MediaTypes.`application/json`) {
              complete {
                val videoList = VMsg(Video.listStaticVideo())
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

object test{
  def main(args: Array[String]) {
    val fileDir = "/Users/soledede/Documents/images"
    val base64S = "iVBORw0KGgoAAAANSUhEUgAAAH0AAABTCAIAAAA9RJ80AAAAzklEQVR4nO3QQREAIAzAMMC/5+GCPGgU9LpnZuW5owM+1Xej70bfjb4bfTf6bvTd6LvRd6PvRt+Nvht9N/pu9N3ou9F3o+9G342+G303+m703ei70Xej70bfjb4bfTf6bvTd6LvRd6PvRt+Nvht9N/pu9N3ou9F3o+9G342+G303+m703ei70Xej70bfjb4bfTf6bvTd6LvRd6PvRt+Nvht9N/pu9N3ou9F3o+9G342+G303+m703ei70Xej70bfjb4bfTf6bvTd6LvRd+MCXBoDo8Qme9oAAAAASUVORK5CYII="
    val fileProcessService = FileProcessService()
    val imageDataStream = new ByteArrayInputStream(Base64.decodeFast(base64S))
    val result = fileProcessService.saveAttachment("testBase.png", imageDataStream, fileDir)
  }
}