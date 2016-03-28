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
import com.soledede.recomend.entity._
import com.soledede.recomend.file.FileProcessService
import com.soledede.recomend.imageSearch.ImageSearchService
import com.soledede.recomend.ui.RecommendUI
import net.liftweb.json.{DateFormat, Formats}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol._
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._
import net.liftweb.json.Serialization._
import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http._
import HttpCharsets._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.util._
import spray.httpx.LiftJsonSupport


import scala.collection.mutable.ListBuffer


/*object MyS extends DefaultJsonProtocol {
  implicit val rmsgFormat = jsonFormat2(RMsg)
}*/

case class RMsg(result: Seq[String], code: Int)

object RMsg {
  implicit val rmsgFormat = jsonFormat2(RMsg.apply)
}

case class RecommendParameters(var userId: Option[String],var catagoryId: Option[String],var brandId:Option[String],var docId: Option[String],var number: Int=0)

object RecommendParameters{
  implicit val recomPFormat = jsonFormat5(RecommendParameters.apply)
}

case class VMsg(result: Seq[Video])

object Video extends Configuration {
  implicit val video = jsonFormat4(Video.apply)

  def listStaticVideo(): Seq[Video] = {

    val videoService = imageHost + ":" + imagePort


    val leastSquareName = "形象理解最小二乘法"
    val leastSquareDesc = "拟合最优超平面，使得误差和最小"
    val leastSquareUrl = "http://" + videoService + "/video/ml-least_square.mp4"
    val leastImg = "http://a1.qpic.cn/psb?/V10sqCyZ06hzDJ/lWpCMe4e0G5ltPhJhlaO0tCS9oC5m3XzqzDWvw5MSGY!/b/dAIBAAAAAAAA&bo=IAPSAQAAAAADB9I!&rf=viewer_4"
    val leastSquareVideo = Video(leastSquareName, leastSquareDesc, leastSquareUrl, leastImg)


    val pictureGradientName = "形象理解梯度下降算法"
    val pictureGradienDesc = "既不能得全局最小值,也得逼近局部最小值"
    val pictureGradienUrl = "http://" + videoService + "/video/picture_gradient.mp4"
    val pictureGradienImg = "http://t.cn/R4GsikL"
    val pictureGradienVideo = Video(pictureGradientName, pictureGradienDesc, pictureGradienUrl, pictureGradienImg)



    val boltzmannCfName = "玻尔兹曼机协同过滤"
    val boltzmannCfDesc = "怎么用深度学习建模评分矩阵"
    val boltzmannCfUrl = "http://" + videoService + "/video/boltzmann_cf.mp4"
    val boltzmannCfImg = "http://a2.qpic.cn/psb?/V10sqCyZ06hzDJ/AGSUYdrRIAd*2p3K69CMCXcDymO41Gb6IY0gWFtWOLM!/b/dP0AAAAAAAAA&bo=IAP6AQAAAAADB*o!&rf=viewer_4"
    val boltzmannCfVideo = Video(boltzmannCfName, boltzmannCfDesc, boltzmannCfUrl, boltzmannCfImg)



    val hmmViterbiDecodeName = "Viterbi解码算法"
    val hmmViterbiDecodeDesc = "信道模型之解码问题"
    val hmmViterbiDecodeUrl = "http://" + videoService + "/video/hmm_viterbi_decode.mp4"
    val hmmViterbiDecodeImg = "http://a3.qpic.cn/psb?/V10sqCyZ06hzDJ/YytiurFA2VyacA9Gi.2xLPWBFaTUMYBHfl51.subhNg!/b/dPsAAAAAAAAA&bo=IAPMAQAAAAADB8w!&rf=viewer_4"
    // val hmmViterbiDecodeVideo = Video(hmmViterbiDecodeName,hmmViterbiDecodeDesc,hmmViterbiDecodeUrl,hmmViterbiDecodeImg)


    val hmmBaumWelchName = "HMM-前向后向算法"
    val hmmBaumWelchDesc = "利用baumwelch求隐变量,利用Dynamic Programming可以大大减少计算量"
    val hmmBaumWelchUrl = "http://" + videoService + "/video/hmm-baum-welch.mp4"
    val hmmBaumWelchImg = "http://a2.qpic.cn/psb?/V10sqCyZ06hzDJ/c3hb4a0v4qAAxeRbYF69E7CpMEaPV7*sPBEJyIfydPE!/b/dKYAAAAAAAAA&bo=IAO.AQAAAAADB74!&rf=viewer_4"
    val hmmBaumWelchVideo = Video(hmmBaumWelchName, hmmBaumWelchDesc, hmmBaumWelchUrl, hmmBaumWelchImg)



    val nativeBayesName = "朴素贝叶斯实现文本分类"
    val nativeBayesDesc = "Bayes实现垃圾邮件过滤,智能聊天系统,主题分类..."
    val nativeBayesUrl = "http://" + videoService + "/video/native_bayes.mp4"
    val nativeBayesimg = "http://a1.qpic.cn/psb?/V10sqCyZ06hzDJ/vnBmrXm2f8OYoRIpXsGEzRgkEndLotRl8qioRj7Tvqs!/b/dAIBAAAAAAAA&bo=IAPEAQAAAAADB8Q!&rf=viewer_4"
    val nativeBayesVideo = Video(nativeBayesName, nativeBayesDesc, nativeBayesUrl, nativeBayesimg)



    val emName = "期望最大化(EM)算法"
    val emDesc = "如果将样本看作观察值，潜在类别看作是隐藏变量那么聚类问题也就是参数估计问题"
    val emUrl = "http://" + videoService + "/video/em.mp4"
    val emImg = "http://a1.qpic.cn/psb?/V10sqCyZ06hzDJ/bv8tPwrLygsnQ5f8SF1T9V5fy4wNItGw.RsF0Arp3Ik!/b/dPwAAAAAAAAA&bo=IAO5AQAAAAADB7k!&rf=viewer_4"
    val emVideo = Video(emName, emDesc, emUrl, emImg)


    val solrItemcfName = "Solr实现ItemCF个性化推荐算法"
    val solrItemcfDesc = "借助lucene的向量空间模型以及solr的facet实现推荐"
    val solrItemcfUrl = "http://" + videoService + "/video/solr_itemcf.mp4"
    val solrItemcfImg = "http://t.cn/R4JasVf"
    val solrItemcfVideo = Video(solrItemcfName, solrItemcfDesc, solrItemcfUrl, solrItemcfImg)


    val recommendIndtroduceName = "推荐系统介绍"
    val recommendIntroduceDesc = "一、推荐系统的介绍,二、大数据介绍及相关环境搭建"
    val recommendIntroduceUrl = "http://" + videoService + "/video/recommend_introduce.mp4"
    val recommendIntroduceImg = "http://a2.qpic.cn/psb?/V10sqCyZ06hzDJ/yfsAI2wh5B4M50pXAg6m5gHEqA8lkkmze1yf9LnzUSw!/b/dKkAAAAAAAAA&bo=IAPFAQAAAAADB8U!&rf=viewer_4"
    val recomndeIntroduceVideo = Video(recommendIndtroduceName, recommendIntroduceDesc, recommendIntroduceUrl, recommendIntroduceImg)


    List(leastSquareVideo, pictureGradienVideo, boltzmannCfVideo, hmmBaumWelchVideo, nativeBayesVideo, emVideo, solrItemcfVideo, recomndeIntroduceVideo)
  }

}

case class Video(name: String, descrition: String, url: String, img: String)

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
  //def receive = runRoute(restRout)
}

/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging with Configuration with spray.httpx.SprayJsonSupport {

  import SprayJsonSupport._

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


  /*path("recommend" / "sku") {

       //
         post {
           entity(Unmarshaller(MediaTypes.`application/json`) {
             case httpEntity: HttpEntity =>
               read[RecommendParameters](httpEntity.asString(HttpCharsets.`UTF-8`))
           }){
             parameters: RecommendParameters =>
               ctx: RequestContext =>
                 handleRequest(ctx) {
                   if (parameters.number == null || parameters.number <= 0 || parameters.number > 80)
                     Right(Msg("推荐数量必须大于0,并且不能超过80个", -1))
                   else Right(defaultRecommendUI.recommendByUserIdOrCatagoryIdOrBrandId(parameters.userId, parameters.catagoryId, parameters.brandId, parameters.number))
                 }
           }
         }
     } ~*/

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
      path("recommends" / Segment / IntNumber) {
        //
        (userId, number) =>
          get {
            ctx: RequestContext =>
              handleRequest(ctx) {
                if (number == null || number <= 0 || number > 80)
                  Right(Msg("推荐数量必须大于0,并且不能超过80个", -1))
                else Right(defaultRecommendUI.recommendByUserId(userId, number))
              }
          }
      } ~
      path("recommend" / Segment / Segment / Segment / Segment / IntNumber) {
        //
        (userId, catagoryId, brandId, docId,number) =>
          get {
            ctx: RequestContext =>
              handleRequest(ctx) {
                if (number == null || number <= 0 || number > 80)
                  Right(Msg("推荐数量必须大于0,并且不能超过80个", -1))
                else Right(defaultRecommendUI.recommendByUserIdOrCatagoryIdOrBrandId(userId, catagoryId, brandId,docId,number))
              }
          }
      } ~
      path("recommend" / "sku") {

        //
        post {
          entity(as[RecommendParameters]){
            parameters: RecommendParameters =>
              ctx: RequestContext =>
                handleRequest(ctx) {
                  if (parameters.number == null || parameters.number <= 0 || parameters.number > 80)
                    Right(Msg("推荐数量必须大于0,并且不能超过80个", -1))
                  else Right(defaultRecommendUI.recommendByUserIdOrCatagoryIdOrBrandId(parameters.userId.getOrElse(null), parameters.catagoryId.getOrElse(null), parameters.brandId.getOrElse(null),parameters.docId.getOrElse(null), parameters.number))
                }
          }
        }
      } ~
      path("mergecloud") {
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              Right(new MergeCloud())
            }
        }
      } ~
      path("mergeclouds") {
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              val list = new ListBuffer[MergeCloud]()
              val m1 = new MergeCloud()
              m1.id = "1"
              m1.isRestrictedArea = 1
              m1.price = 2452.4
              m1.categoryId3 = 1001739
              m1.t89_s = "百事通"
              m1.t87_tf = 100
              list += m1
              val m2 = new MergeCloud()
              m2.id = "2"
              m2.title = "奔驰轮胎,车主可随叫随到"
              m2.cityId = "4_5_9_243"
              m2.isRestrictedArea = 1
              m2.price = 2
              m2.categoryId3 = 1001739
              m2.t89_s = "世达"
              m2.t87_tf = -19
              list += m2
              val m3 = new MergeCloud()
              m3.id = "3"
              m3.cityId = "321_34_135_5"
              m3.title = "碳化火钳"
              m3.isRestrictedArea = 1
              m3.price = 349
              m3.categoryId4 = 1001739
              m3.t89_s = "世达"
              m3.t87_tf = 4
              list += m3
              val m4 = new MergeCloud()
              m4.id = "4"
              m4.isRestrictedArea = 1
              m4.price = 245
              m4.title = "螺旋式流花玻璃"
              m4.categoryId3 = 1001739
              m4.t89_s = "百事通"
              m4.t87_tf = 34
              list += m4
              val m5 = new MergeCloud()
              m5.id = "5"
              m5.title = "橡胶塑料袋10袋起卖"
              m5.cityId = "2_3562_42_90"
              m5.isRestrictedArea = 1
              m5.price = 98
              m5.categoryId3 = 1001739
              m5.t89_s = "世达"
              m5.t87_tf = 2
              list += m5
              val m6 = new MergeCloud()
              m6.id = "6"
              m6.cityId = "564"
              m6.title = "防寒马甲"
              m6.isRestrictedArea = 1
              m6.price = 84
              m6.categoryId3 = 1001739
              m6.t89_s = "福大"
              m6.t87_tf = 45
              list += m6

              val m7 = new MergeCloud()
              m7.id = "7"
              m7.price = 12
              m7.title = "防护眼睛配件"
              m7.categoryId4 = 1001739
              list += m7
              val m8 = new MergeCloud()
              m8.id = "8"
              m8.price = 30
              m8.title = "六角电锤"
              m8.categoryId3 = 1001739
              list += m8
              val m9 = new MergeCloud()
              m9.id = "9"
              m9.price = 19
              m9.title = "六角螺丝刀"
              m9.categoryId3 = 1001739
              m9.t89_s = "soledede"
              m9.t87_tf = 30
              list += m9

              val m10 = new MergeCloud()
              m10.id = "10"
              m10.price = 34
              m10.categoryId3 = 1001739
              m10.t89_s = "memmert"
              m10.title = "龙珠泡11w"
              m10.t87_tf = 36
              list += m10

              val m11 = new MergeCloud()
              m11.id = "11"
              m11.price = 3.4
              m11.title = "卤素灯1000w"
              m11.categoryId3 = 1001739
              m11.t89_s = "Memmert"
              m11.t87_tf = 14
              list += m11

              val m12 = new MergeCloud()
              m12.id = "12"
              m12.brandId = 8724
              m12.brandEn = "ie"
              m12.brandZh = "世达"
              m12.title = "带表量仪"
              list += m12

              val m13 = new MergeCloud()
              m13.id = "13"
              m13.brandId = 125
              m13.brandEn = "kwe"
              m13.brandZh = "蟋蟀"
              m13.title = "daide"
              list += m13

              val m14 = new MergeCloud()
              m14.id = "14"
              m14.brandId = 125
              m14.brandEn = "kwe"
              m14.brandZh = "蟋蟀"
              m14.title = "带灯按钮"
              list += m14

              val m15 = new MergeCloud()
              m15.id = "15"
              m15.brandId = 125
              m15.brandEn = "kwe"
              m15.brandZh = "蟋蟀"
              m15.title = "带盖尿液收集试管,带阀气缸,带护锥中心钻"
              list += m15


              Right(list)
            }
        }
      } ~
      path("screenclouds") {
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              val list = new ListBuffer[ScreenCloud]()
              val s1 = new ScreenCloud()
              list += s1
              val s2 = new ScreenCloud()
              s2.id = "1003484_t87_s"
              s2.filterId_s = "t87_tf"
              s2.attDescZh_s = "规格"
              s2.range_s = "0-10|10-20|20-30"
              list += s2
              Right(list)
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
      } ~
      path("soledede" / "sku") {
        detach() {
          post {
            respondWithMediaType(MediaTypes.`application/json`) {
              entity(as[RecommendParameters]) { parameters =>
                complete {
                  try {
                      println(parameters)
                    RMsg(List(), -1)
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

object test {
  def main(args: Array[String]) {
    val fileDir = "/Users/soledede/Documents/images"
    val base64S = "iVBORw0KGgoAAAANSUhEUgAAAH0AAABTCAIAAAA9RJ80AAAAzklEQVR4nO3QQREAIAzAMMC/5+GCPGgU9LpnZuW5owM+1Xej70bfjb4bfTf6bvTd6LvRd6PvRt+Nvht9N/pu9N3ou9F3o+9G342+G303+m703ei70Xej70bfjb4bfTf6bvTd6LvRd6PvRt+Nvht9N/pu9N3ou9F3o+9G342+G303+m703ei70Xej70bfjb4bfTf6bvTd6LvRd6PvRt+Nvht9N/pu9N3ou9F3o+9G342+G303+m703ei70Xej70bfjb4bfTf6bvTd6LvRd+MCXBoDo8Qme9oAAAAASUVORK5CYII="
    val fileProcessService = FileProcessService()
    val imageDataStream = new ByteArrayInputStream(Base64.decodeFast(base64S))
    val result = fileProcessService.saveAttachment("testBase.png", imageDataStream, fileDir)
  }
}