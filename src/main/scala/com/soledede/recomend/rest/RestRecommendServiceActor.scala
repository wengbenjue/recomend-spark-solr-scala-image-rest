package com.soledede.recomend.rest

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.dao.HbaseRecommendModel
import com.soledede.recomend.domain.Failure
import java.text.{ParseException, SimpleDateFormat}
import java.util.Date
import com.soledede.recomend.entity.Msg
import com.soledede.recomend.ui.RecommendUI
import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
import scala.Some
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._




/**
  *
  * REST Service actor.
  */
class RestRecommendServiceActor extends Actor with RestService {

  implicit def actorRefFactory = context

  def receive = runRoute(rest)
}

/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging {


  val resModuleService = HbaseRecommendModel()

  val defaultRecommendUI = RecommendUI()

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
    path("recommend" / LongNumber) {
      userId =>
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              log.debug("Retrieving customer with id %d".format(userId))
              log.debug("test")
              log.info("haha")
              println("进来了!")
              Right(Msg("zhong像素"))
              //resModuleService.get(userId.toString)
            }
        }
    }~
    path("recommend" / Segment /IntNumber) {
      (userId,number) =>
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              Right(defaultRecommendUI.recommendByUserId(userId,number))
            }
        }
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