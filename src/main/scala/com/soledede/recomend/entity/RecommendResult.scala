package com.soledede.recomend.entity

import java.util.regex.Pattern

import akka.util.ByteString
import redis.ByteStringFormatter

/**
  * Created by soledede on 2015/12/17.
  */
case class RecommendResult(var item: String, var weight: Double) extends Serializable {
  def this() = this(null, null)
}

object RecommendResult {
  private val Seperator = "#&#&#"
  implicit val byteStringFormatter = new ByteStringFormatter[RecommendResult] {
    def serialize(recomend: RecommendResult): ByteString = {
      ByteString(
        recomend.item + Seperator +
        recomend.weight + Seperator
      )
    }

    def deserialize(bs: ByteString): RecommendResult = {
      val r = bs.utf8String.split(Pattern.quote(Seperator)).toList
      val recommend = new RecommendResult()
      recommend.item = r(0)
      recommend.weight = r(1).toDouble
      recommend
    }
  }
}