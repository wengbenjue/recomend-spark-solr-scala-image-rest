package com.soledede.recomend.cache.impl


import akka.event.slf4j.SLF4JLogging
import akka.util.ByteString
import com.soledede.recomend.cache.KVCache
import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.redis.Redis
import com.soledede.recomend.rest.VMsg
import redis.{ByteStringFormatter, ByteStringDeserializer, ByteStringSerializer}
import net.liftweb.json._
import net.liftweb.json.Serialization.{write, read}

import scala.reflect.ClassTag

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





  private val Seperator = "#&#&#"
  implicit val stringFormatter = new ByteStringFormatter[Seq[String]] {
    def serialize(vS: Seq[String]): ByteString = {
      ByteString(
        write(vS)
      )
    }

    def deserialize(bs: ByteString): Seq[String] = {
      val r = bs.utf8String
      read[Seq[String]](r)
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

  override def getCache[T: ClassTag](key: String): T = {
    val oR = redis.getValue[String](key)
    if (oR == null) null.asInstanceOf[T]
    else oR.getOrElse(null).asInstanceOf[T]
  }

  override def putCache[T: ClassTag](key: String, value: T, expiredTime: Long): Boolean = {
    redis.setValue[String](key, value.toString, expiredTime)
  }

  override def putStringList(key: String, value: Seq[String], expiredTime: Long): Boolean = {
    redis.setValue[Seq[String]](key, value, expiredTime)
  }

  override def getStringList(key: String): Seq[String] = {
    val oR = redis.getValue[Seq[String]](key)
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

