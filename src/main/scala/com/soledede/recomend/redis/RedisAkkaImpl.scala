package com.soledede.recomend.redis

import akka.event.slf4j.SLF4JLogging
import redis.{ByteStringDeserializer, ByteStringSerializer}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by soledede on 2015/12/18.
  */
class RedisAkkaImpl private extends Redis with SLF4JLogging {

  implicit val akkaSystem = akka.actor.ActorSystem()
  val redis = RedisClient()

  override def putToSortedSet[T: ByteStringSerializer](name: String, value: T, score: Double): Boolean = {
    this.synchronized {
      var r = false
      try {
        val result = redis.zadd(name, (score, value))
        Await.result(result, 10 seconds)
        if (result.value.get.get == 1) r = true
      } catch {
        case e: Exception => log.error("put data to redis sortedset failed！" + e.getMessage(), e)
      }
      r
    }
  }

  override def getFirstFromSortedSet[T: ByteStringDeserializer](name: String): (T, Double) = {
    var rsObj = null.asInstanceOf[T]
    var scores: Double = 0.0
    try {
      val r = for {
        getOpt <- redis.zrangeWithscores[T](name, 0, 0)
      } yield {
        getOpt.map(obj => {
          rsObj = obj._1
          scores = obj._2
        })
      }
      Await.result(r, 10 seconds)
    } catch {
      case e: Exception => log.error("get first data from redis sortedset failed！", e)
    }
    (rsObj, scores)
  }

  def getLastFromSortedSet[T: ByteStringDeserializer](name: String): (T, Double) = {
    var rsObj = null.asInstanceOf[T]
    var scores: Double = 0.0
    try {
      val r = for {
        getOpt <- redis.zrangeWithscores[T](name, -1, -1)
      } yield {
        getOpt.map(obj => {
          rsObj = obj._1
          scores = obj._2
        })
      }
      Await.result(r, 10 seconds)
    } catch {
      case e: Exception => log.error("get last data from redis sortedset failed！" + e.getMessage(), e)
    }
    (rsObj, scores)
  }

  override def rmLastFromSortedSet(key: String): Boolean = {
    this.synchronized {
      var r = false
      try {
        val result = redis.zremrangebyrank(key, -1, -1)
        Await.result(result, 10 seconds)
        if (result.value.get.get == 1) r = true
      } catch {
        case e: Exception => log.error("del last data from redis sortedset failed！" + e.getMessage(), e)
      }
      r
    }
  }

  override def rmFirstFromSortedSet(key: String): Boolean = {
    this.synchronized {
      var r = false
      try {
        val result = redis.zremrangebyrank(key, 0, 0)
        Await.result(result, 10 seconds)
        if (result.value.get.get == 1) r = true
      } catch {
        case e: Exception => log.error("del first data from redis sortedset failed！" + e.getMessage(), e)
      }
      r
    }
  }

  override def putToList[T: ByteStringSerializer](key: String, values: T): Boolean = {
    this.synchronized {
      var r = false
      try {
        val result = redis.lpush[T](key, values)
        if (result.value.get.get == 1) r = true
        Await.result(result, 10 seconds)
        println(result)
      } catch {
        case e: Exception => log.error("put data to redis list failed！ " + e.getMessage, e)
      }
      r
    }
  }

  override def popFirstFromList[T: ByteStringDeserializer](key: String): T = {
    var rsObj = null.asInstanceOf[T]
    try {
      val r = for {
        getOpt <- redis.lpop[T](key)
      } yield {
        getOpt.map(getObj => {
          rsObj = getObj
        })
      }
      Await.result(r, 10 seconds)
    } catch {
      case e: Exception => log.error("get first data from redis list failed！" + e.getMessage(), e)
    }
    rsObj
  }

  def popLastFromList[T: ByteStringDeserializer](key: String): T = {
    var rsObj = null.asInstanceOf[T]
    try {
      val r = for {
        getOpt <- redis.rpop[T](key)
      } yield {
        getOpt.map(getObj => {
          rsObj = getObj
        })
      }
      Await.result(r, 10 seconds)
    } catch {
      case e: Exception => log.error("get last data from redis list failed！" + e.getMessage(), e)
    }
    rsObj
  }

  def getCounterNextVal(key: String): Long = {
    this.synchronized {
      var r = -1L
      try {
        val result = redis.incr(key)
        Await.result(result, 10 seconds)
        r = result.value.get.get
      } catch {
        case e: Exception => log.error("get counter next value failed！" + e.getMessage(), e)
      }
      r
    }
  }

  def putKvHashMap[T: ByteStringSerializer](key: String, keysValues: Map[String, T]): Boolean = {
    this.synchronized {
      var r = false
      try {
        val result = redis.hmset(key, keysValues)
        Await.result(result, 10 seconds)
        r = result.value.get.get
      } catch {
        case e: Exception => log.error("put key-value HashMap to Redis failed！" + e.getMessage(), e)
      }
      r
    }
  }

  def getKvHashMap(key: String, fields: Seq[String]): Seq[String] = {
    var r = null.asInstanceOf[Seq[String]]
    try {
      val result = redis.hmget(key, fields: _*)
      Await.result(result, 10 seconds)
      r = result.value.get.get.map { ele => ele.get.utf8String }
    } catch {
      case e: Exception => log.error("get key-value HashMap from Redis failed！" + e.getMessage(), e)
    }
    r
  }

  def getHashmapByKeyField[T: ByteStringDeserializer](key: String, fields: Seq[String]): Seq[Option[T]] = {
    var r = null.asInstanceOf[Seq[Option[T]]]
    try {
      val result = redis.hmget(key, fields: _*)
      Await.result(result, 10 seconds)
      r = result.value.get.get
    } catch {
      case e: Exception => log.error("get key-value HashMap from Redis failed！" + e.getMessage(), e)
    }
    r
  }

  def getHashmapByKeyField2[T: ByteStringDeserializer](key: String, fields: Seq[String]): Seq[Option[T]] = {
    var r = null.asInstanceOf[Seq[Option[T]]]
    try {
      val result = redis.hmget(key, fields: _*)
      Await.result(result, 10 seconds)
      r = result.value.get.get
    } catch {
      case e: Exception => log.error("get key-value HashMap from Redis failed！" + e.getMessage(), e)
    }
    r
  }

  def setValue[T: ByteStringSerializer](key: String, value: T): Boolean = {
    this.synchronized {
      var r = null.asInstanceOf[Boolean]
      try {
        val result = redis.set(key, value)
        Await.result(result, 500 milliseconds)
        r = result.value.get.get
        log.debug("set (" + key + " -> " + value + ")")
      } catch {
        case e: Exception => log.error("set key -> value failed！", e)
      }
      r
    }
  }

  // redis command: SETEX key seconds value
  def setValue[T: ByteStringSerializer](key: String, value: T, exSeconds: Long): Boolean = {
    this.synchronized {
      var r = null.asInstanceOf[Boolean]
      try {
        val result = redis.set(key, value, Option(exSeconds))
        Await.result(result, 1 milliseconds)
        r = result.value.get.get
        log.debug("set (" + key + " -> " + value + ")")
      } catch {
        case e: Exception => log.error("set key -> value failed！", e.getMessage)
      }
      r
    }
  }

  def getValue[T: ByteStringDeserializer](key: String): Option[T] = {
    var r = null.asInstanceOf[Option[T]]
    try {
      val result = redis.get(key)
      Await.result(result, 15 milliseconds)
      r = result.value.get.get
      log.debug("get (" + key + " -> " + r + ")")
    } catch {
      case e: Exception => log.error("get key -> value failed！", e.getMessage)
    }
    r
  }

  def delKey(keys: Seq[String]): Long = {
    this.synchronized {
      var r = null.asInstanceOf[Long]
      try {
        val result = redis.del(keys: _*)
        Await.result(result, 10 seconds)
        r = result.value.get.get
        log.debug("del " + keys)
      } catch {
        case e: Exception => log.error("del keys failed！", e)
      }
      r
    }
  }


  def putToSet[T: ByteStringSerializer](key: String, member: T): Boolean = {
    this.synchronized {
      var r = null.asInstanceOf[Boolean]
      try {
        val result = redis.sadd(key, member)
        Await.result(result, 10 seconds)
        if (result.value.get.get == 1) r = true
        log.debug("sadd (" + key + " -> " + member + ")")
      } catch {
        case e: Exception => log.error("set key -> value failed！", e)
      }
      r
    }
  }

  def removeElementFromSetByKey[T: ByteStringSerializer](key: String, members: Seq[T]): Boolean = {
    this.synchronized {
      var r = null.asInstanceOf[Boolean]
      try {
        val result = redis.srem(key, members: _*)
        Await.result(result, 10 seconds)
        if (result.value.get.get == 1) r = true
        log.debug("srem (" + key + " -> " + members + ")")
      } catch {
        case e: Exception => log.error("srem key -> value failed！", e)
      }
      r
    }
  }

  def getAllFromSetByKey[T: ByteStringDeserializer](key: String): Seq[T] = {
    // this.synchronized {
    var r: Seq[T] = null
    try {
      val result = redis.smembers(key)
      Await.result(result, 10 seconds)
      r = result.value.get.get
      log.debug("get (" + key + " -> " + r + ")")
    } catch {
      case e: Exception => log.error("get key -> value failed！", e)
        null
    }
    r
    //}
  }


  override def incrBy(key: String, step: Int): Int = {
    this.synchronized {
      var r = -1
      try {
        val result = redis.incrby(key, step)
        Await.result(result, 20 seconds)
        r = result.value.get.get.toInt
      } catch {
        case e: Exception => log.error("get counter by step value failed！" + e.getMessage(), e)
      }
      r
    }
  }

  def keys(parttern: String): Option[Seq[String]] = {
    var r: Option[Seq[String]] = None
    try {
      val result = redis.keys(parttern)
      Await.result(result, 20 seconds)
      r = result.value.get.toOption
    } catch {
      case e: Exception => log.error("get counter by step value failed！" + e.getMessage(), e)
    }
    r
  }

}

object RedisAkkaImpl {
  var redisAkka: RedisAkkaImpl = null

  def apply(): RedisAkkaImpl = {
    if (redisAkka == null)
      redisAkka = new RedisAkkaImpl()
    redisAkka
  }
}