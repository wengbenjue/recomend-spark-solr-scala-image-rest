package com.soledede.recomend.cache

import com.soledede.recomend.cache.impl.RedisCache
import com.soledede.recomend.entity.RecommendResult

import scala.reflect.ClassTag


/**
  * Created by soledede on 2015/12/18.
  */
trait KVCache {

  def put(key: String, value: Seq[RecommendResult], expiredTime: Long = 60 * 60 * 17): Boolean = false

  def get(key: String): Seq[RecommendResult] = null

  def getCache[T: ClassTag](key: String): T = null.asInstanceOf[T]

  def putCache[T: ClassTag](key: String, value: T, expiredTime: Long = 60 * 60 * 17): Boolean = false

}

object KVCache {
  def apply(kvType: String = "redis"): KVCache = {
    kvType match {
      case "redis" => RedisCache()
      case _ => null
    }

  }
}
