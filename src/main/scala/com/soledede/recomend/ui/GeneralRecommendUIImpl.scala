package com.soledede.recomend.ui

import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.service.RecommendService

/**
  * Created by soledede on 2015/12/15.
  */
class GeneralRecommendUIImpl extends RecommendUI {

  val solrRecommendCFService = RecommendService()
  val solrRecommendCTService = RecommendService("solrCT")

  override def recommendByUserId(userId: String, number: Int): Seq[RecommendResult] = {
    solrRecommendCFService.recommendByUserId(userId, number)
  }

  override def recommendMostLikeCatagoryIdByKeywords(keyword: String): String = solrRecommendCTService.recommendMostLikeCatagoryIdByKeywords(keyword)
}

object GeneralRecommendUIImpl {
  var generalRecommendUIImpl: GeneralRecommendUIImpl = null

  def apply(): GeneralRecommendUIImpl = {
    if (generalRecommendUIImpl == null) generalRecommendUIImpl = new GeneralRecommendUIImpl()
    generalRecommendUIImpl
  }
}