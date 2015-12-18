package com.soledede.recomend.cache.impl


import akka.event.slf4j.SLF4JLogging
import akka.util.ByteString
import com.soledede.recomend.cache.KVCache
import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.redis.Redis
import redis.{ByteStringFormatter, ByteStringDeserializer, ByteStringSerializer}
import net.liftweb.json._
import net.liftweb.json.Serialization.{write, read}

/**
  * Created by soledede on 2015/12/18.
  */
class RedisCache private extends KVCache with SLF4JLogging {
  val redis = Redis()
  //implicit val formats = Serialization.formats(NoTypeHints)
  implicit val formats = DefaultFormats

  implicit val byteStringFormatter = new ByteStringFormatter[Seq[RecommendResult]] {
    def serialize(recomends: Seq[RecommendResult]): ByteString = {
      ByteString(
        write(recomends)
      )
    }

    def deserialize(bs: ByteString): Seq[RecommendResult] = {
      val r = bs.utf8String
      read[List[RecommendResult]](r)
    }
  }

  override def put(key: String, value: Seq[RecommendResult], expiredTime: Long): Boolean = {
    redis.setValue[Seq[RecommendResult]](key, value, expiredTime)
  }

  override def get(key: String): Seq[RecommendResult] = {
    val oR = redis.getValue[Seq[RecommendResult]](key)
    if (oR == null) null
    else oR.getOrElse(null)

  }
}

object RedisCache {
  var redisCache: RedisCache = null

  def apply(): RedisCache = {
    if (redisCache == null) redisCache = new RedisCache
    redisCache
  }

}

