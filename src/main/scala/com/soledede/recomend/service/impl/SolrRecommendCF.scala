package com.soledede.recomend.service.impl


import com.soledede.recomend.service.RecommendService
import com.soledede.recomend.solr.SolrClient

/**
  * Created by soledede on 2015/12/15.
  */
class SolrRecommendCF private extends RecommendService {
  val solrClient = SolrClient()

  override def recommendByUserId(userId: Int): Seq[String] = {
    //TODO search docs by userid
    //TODO search k-neighbour of userid by docs that boosted
    // TODO search recommend docs by users that boosted but not(docs that cuurent userid purchased)
  }
}

object SolrRecommendCF {
  var solrCf: SolrRecommendCF = null
  def apply(): SolrRecommendCF = {
    if (solrCf == null) solrCf = new SolrRecommendCF()
    solrCf
  }
}
