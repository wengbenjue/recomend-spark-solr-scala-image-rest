package com.soledede.recomend.ui

import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.service.RecommendService

/**
  * Created by soledede on 2015/12/15.
  */
class GeneralRecommendUIImpl extends RecommendUI {

  val solrRecommendCFService = RecommendService()
  val solrRecommendCTService = RecommendService("solrCT")
  val solrRecommendMoreLikeThisService  = RecommendService("moreLikeThis")

  val generalUseId = "null"

  override def recommendByUserId(userId: String, number: Int): Seq[RecommendResult] = {
    solrRecommendCFService.recommendByUserId(userId, number)
  }

  override def recommendMostLikeCatagoryIdByKeywords(keyword: String): String = solrRecommendCTService.recommendMostLikeCatagoryIdByKeywords(keyword)

  override def recommendByUserIdOrCatagoryIdOrBrandId(userId: String, catagoryId: String, brandId: String, number: Int): Seq[RecommendResult] = {
    if (userId != null && !userId.trim.equalsIgnoreCase("") && !userId.trim.equalsIgnoreCase("null")) solrRecommendCFService.recommendByUserId(userId, number)
    else if (catagoryId != null && !catagoryId.trim.equalsIgnoreCase("") && !catagoryId.trim.equalsIgnoreCase("null")) {
      val cRecommends = solrRecommendMoreLikeThisService.recommendByCatagoryId(catagoryId, number)
      if (cRecommends == null) solrRecommendCFService.recommendByUserId(generalUseId, number)
      else cRecommends
    } else if (brandId != null && !brandId.trim.equalsIgnoreCase("") && !brandId.trim.equalsIgnoreCase("null")) {
      val bRecommends = solrRecommendMoreLikeThisService.recommendByBrandId(brandId, number)
      if (bRecommends == null) solrRecommendCFService.recommendByUserId(generalUseId, number)
      else bRecommends
    } else solrRecommendCFService.recommendByUserId(generalUseId, number)
  }
}

object GeneralRecommendUIImpl {
  var generalRecommendUIImpl: GeneralRecommendUIImpl = null

  def apply(): GeneralRecommendUIImpl = {
    if (generalRecommendUIImpl == null) generalRecommendUIImpl = new GeneralRecommendUIImpl()
    generalRecommendUIImpl
  }
}