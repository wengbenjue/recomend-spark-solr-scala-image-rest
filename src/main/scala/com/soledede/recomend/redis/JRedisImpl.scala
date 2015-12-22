package com.soledede.recomend.redis

import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.config.Configuration

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/21.
  */
class JRedisImpl extends Redis with SLF4JLogging {
  //general
/*  override def setValue[T: ClassTag](key: String, value: T, exSeconds: Long): Boolean = ???

  override def getValue[T: ClassTag](key: String): T = ???*/
}

object JRedisImpl extends Configuration {

}
