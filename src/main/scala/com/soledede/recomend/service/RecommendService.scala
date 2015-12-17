package com.soledede.recomend.service

import com.soledede.recomend.service.impl.SolrRecommendCF

/**
  * Created by soledede on 2015/12/15.
  */
trait RecommendService {
  def recommendByUserId(userId: String,number: Int): Seq[String] = null
}

object RecommendService {
  def apply(name: String = "solrCF"): RecommendService = {
    name match {
      case "solrCF" => SolrRecommendCF()
      case "solrCT" => null.asInstanceOf[RecommendService]
    }
  }
}
