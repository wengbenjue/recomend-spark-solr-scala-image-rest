package com.soledede.recomend.ui

import com.soledede.recomend.entity.{RecommendResult, User}

/**
  * Created by soledede on 2015/12/15.
  */
trait RecommendUI {

  def recommendByUser(user: User,number:Int): Seq[String] = null

  def recommendByUserId(userId: String,number:Int): Seq[RecommendResult] = null

  def recommendByCatagoryByUser(user: User,number:Int): Seq[String] = null

  def recommendMostLikeCatagoryIdByKeywords(keyword: String): String = null

  def recommendByUserIdOrCatagoryIdOrBrandId(userId: String,catagoryId: String,brandId: String,docId: String,number:Int):Seq[RecommendResult] = null

}

object RecommendUI{
  def apply(rType: String = "default"): RecommendUI = {
    rType match {
      case "default" => GeneralRecommendUIImpl()
    }
  }
}
