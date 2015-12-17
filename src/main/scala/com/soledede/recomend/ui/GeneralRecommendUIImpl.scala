package com.soledede.recomend.ui

import com.soledede.recomend.service.RecommendService

/**
  * Created by soledede on 2015/12/15.
  */
class GeneralRecommendUIImpl extends RecommendUI {

  val solrRecommendService = RecommendService()


  override def recommendByUserId(userId: String, number: Int): Seq[String] = {
    solrRecommendService.recommendByUserId(userId, number)
  }
}

object GeneralRecommendUIImpl {
  var generalRecommendUIImpl: GeneralRecommendUIImpl = null

  def apply(): GeneralRecommendUIImpl = {
    if (generalRecommendUIImpl == null) generalRecommendUIImpl = new GeneralRecommendUIImpl()
    generalRecommendUIImpl
  }
}