package com.soledede.recomend.cache.impl

import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.cache.KVCache
import com.soledede.recomend.redis.Redis

/**
  * Created by soledede on 2015/12/18.
  */
class RedisCache extends KVCache with SLF4JLogging{
  val redis =  Redis()

  override def put(key: String, value: Any, expiredTime: Long): Boolean = {

  }

  override def get(key: String): Any = ???
}
