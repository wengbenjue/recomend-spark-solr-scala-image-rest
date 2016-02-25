package com.soledede.recomend.service

import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.service.impl.{SolrRecommendMoreLikeThis, SolrRecommendCategory, SolrRecommendCF}

/**
  * Created by soledede on 2015/12/15.
  */
trait RecommendService {
  def recommendByUserId(userId: String, number: Int): Seq[RecommendResult] = null

  def recommendMostLikeCatagoryIdByKeywords(keywords: String): String = null

  def recommendByCatagoryId(catagoryId: String, number: Int): Seq[RecommendResult] = null

  def recommendByBrandId(brandId: String, number: Int): Seq[RecommendResult] = null
}

object RecommendService {
  def apply(name: String = "solrCF"): RecommendService = {
    name match {
      case "solrCF" => SolrRecommendCF()
      case "solrCT" => SolrRecommendCategory()
      case "moreLikeThis" => SolrRecommendMoreLikeThis()
      case _ => null
    }
  }
}
