package com.soledede.recomend.boot

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.soledede.recomend.config.Configuration
import com.soledede.recomend.rest.RestRecommendServiceActor
import spray.can.Http

object Boot extends App with Configuration {

  // create an actor system for application
  implicit val system = ActorSystem("rest-service-recomend")

  // create and start rest service actor
  val restService = system.actorOf(Props[RestRecommendServiceActor], "rest-endpoint")

  // start HTTP server with rest service actor as a handler
  IO(Http) ! Http.Bind(restService, serviceHost, servicePort)
}