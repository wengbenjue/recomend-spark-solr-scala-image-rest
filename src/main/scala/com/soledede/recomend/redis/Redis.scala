package com.soledede.recomend.redis

import redis.{ByteStringDeserializer, ByteStringSerializer}

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/18.
  */
trait Redis {
  def putToSortedSet[T: ByteStringSerializer](name: String, value: T, score: Double): Boolean = false

  def getFirstFromSortedSet[T: ByteStringDeserializer](name: String): (T, Double) = null

  def getLastFromSortedSet[T: ByteStringDeserializer](name: String): (T, Double) = null

  def putToList[T: ByteStringSerializer](key: String, values: T): Boolean = false

  def popFirstFromList[T: ByteStringDeserializer](key: String): T = null.asInstanceOf[T]

  def popLastFromList[T: ByteStringDeserializer](key: String): T = null.asInstanceOf[T]

  def rmFirstFromSortedSet(key: String): Boolean = false

  def rmLastFromSortedSet(key: String): Boolean = false

  def getCounterNextVal(key: String): Long = -1L

  def putKvHashMap[T: ByteStringSerializer](key: String, keysValues: Map[String, T]): Boolean = false

  def getKvHashMap(key: String, fields: Seq[String]): Seq[String] = null.asInstanceOf[Seq[String]]

  def getHashmapByKeyField[T: ByteStringDeserializer](key: String, fields: Seq[String]): Seq[Option[T]] = null.asInstanceOf[Seq[Option[T]]]

  def setValue[T: ByteStringSerializer](key: String, value: T): Boolean = false

  def setValue[T: ByteStringSerializer](key: String, value: T, exSeconds: Long): Boolean = false

  def getValue[T: ByteStringDeserializer](key: String): Option[T] = null.asInstanceOf[Option[T]]

  def delKey(keys: Seq[String]): Long = -1L

  def putToSet[T: ByteStringSerializer](key: String, members: T): Boolean = false

  def removeElementFromSetByKey[T: ByteStringSerializer](key: String, members: Seq[T]): Boolean = false

  def getAllFromSetByKey[T: ByteStringDeserializer](key: String): Seq[T] = null.asInstanceOf[Seq[T]]

  def incrBy(key: String, step: Int): Int = -1

  def keys(parttern: String): Option[Seq[String]] = null.asInstanceOf[Option[Seq[String]]]

  //general
/*  def setValue[T: ClassTag](key: String, value: T, exSeconds: Long): Boolean = false


  def getValue[T: ClassTag](key: String): T = null.asInstanceOf[T]*/

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