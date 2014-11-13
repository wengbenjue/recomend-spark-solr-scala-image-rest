package com.xiaomishu.recomend.rest

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.sysgears.recomend.dao.ModuleDAO
import com.xiaomishu.recomend.domain._
import java.text.{ParseException, SimpleDateFormat}
import java.util.Date
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
class RestServiceActor extends Actor with RestService {

  implicit def actorRefFactory = context

  def receive = runRoute(rest)
}

/**
 * REST Service
 */
trait RestService extends HttpService with SLF4JLogging {

  val resModuleService =  new ModuleDAO()

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

  implicit val customRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse {
      response =>
        response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`),
          write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  val rest = respondWithMediaType(MediaTypes.`application/json`) {
    /**
    path("customer") {
      post {
        entity(Unmarshaller(MediaTypes.`application/json`) {
          case httpEntity: HttpEntity =>
            read[Customer](httpEntity.asString(HttpCharsets.`UTF-8`))
        }) {
          customer: Customer =>
            ctx: RequestContext =>
              handleRequest(ctx, StatusCodes.Created) {
                log.debug("Creating customer: %s".format(customer))
                resModuleService.create(customer)
              }
        }
      } ~
        get {
          parameters('firstName.as[String] ?, 'lastName.as[String] ?, 'birthday.as[Date] ?).as(CustomerSearchParameters) {
            searchParameters: CustomerSearchParameters => {
              ctx: RequestContext =>
                handleRequest(ctx) {
                  log.debug("Searching for customers with parameters: %s".format(searchParameters))
                  resModuleService.search(searchParameters)
                }
            }
          }
        }
    } ~
    **/
    //"res" / Segment 匹配字符串
      path("res" / LongNumber) {
        userId =>
          put {
            entity(Unmarshaller(MediaTypes.`application/json`) {
              case httpEntity: HttpEntity =>
                read[Customer](httpEntity.asString(HttpCharsets.`UTF-8`))
            }) {
              customer: Customer =>
                ctx: RequestContext =>
                  handleRequest(ctx) {
                    log.debug("Updating customer with id %d: %s".format(0, customer))
                    resModuleService.update(0, customer)
                  }
            }
          } ~
            delete {
              ctx: RequestContext =>
                handleRequest(ctx) {
                  log.debug("Deleting customer with id %d".format(0))
                  resModuleService.delete(0)
                }
            } ~
            get {
              ctx: RequestContext =>
                handleRequest(ctx) {
                 // log.debug("Retrieving customer with id %d".format(userId))
                  resModuleService.get(userId.toString)
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