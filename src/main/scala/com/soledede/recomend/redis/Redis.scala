package com.soledede.recomend.redis

import redis.{ByteStringDeserializer, ByteStringSerializer}

/**
  * Created by soledede on 2015/12/18.
  */
trait Redis {
  def putToSortedSet[T: ByteStringSerializer](name: String, value: T, score: Double): Boolean

  def getFirstFromSortedSet[T: ByteStringDeserializer](name: String): (T, Double)

  def getLastFromSortedSet[T: ByteStringDeserializer](name: String): (T, Double)

  def putToList[T: ByteStringSerializer](key: String, values: T): Boolean

  def popFirstFromList[T: ByteStringDeserializer](key: String): T

  def popLastFromList[T: ByteStringDeserializer](key: String): T

  def rmFirstFromSortedSet(key: String): Boolean

  def rmLastFromSortedSet(key: String): Boolean

  def getCounterNextVal(key: String): Long

  def putKvHashMap[T: ByteStringSerializer](key: String, keysValues: Map[String, T]): Boolean

  def getKvHashMap(key: String, fields: Seq[String]): Seq[String]

  def getHashmapByKeyField[T: ByteStringDeserializer](key: String, fields: Seq[String]): Seq[Option[T]]

  def setValue[T: ByteStringSerializer](key: String, value: T): Boolean

  def setValue[T: ByteStringSerializer](key: String, value: T, exSeconds: Long): Boolean

  def getValue[T: ByteStringDeserializer](key: String): Option[T]

  def delKey(keys: Seq[String]): Long

  def putToSet[T: ByteStringSerializer](key: String, members: T): Boolean

  def removeElementFromSetByKey[T: ByteStringSerializer](key: String, members: Seq[T]): Boolean

  def getAllFromSetByKey[T: ByteStringDeserializer](key: String): Seq[T]

  def incrBy(key: String, step: Int): Int

  def keys(parttern: String): Option[Seq[String]]
}

object Redis {

  def apply(redisOpsType: String = "akka"): Redis = {
    createRedis(redisOpsType)
  }

  def createRedis(redisOpsType: String): Redis = {
    redisOpsType match {
      case "akka" => RedisAkkaImpl()
      case _ => null
    }
  }
}