package com.soledede.recomend.service.impl

import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.cache.KVCache
import com.soledede.recomend.service.RecommendService
import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Created by soledede on 2015/12/22.
  */
class SolrRecommendCategory private extends RecommendService with SLF4JLogging {
  val cache = KVCache()
  val solrClient = SolrClient()
  val keywordPreffiex = "search_keyword_cache_"

  def getCatagoryByCache(keywords: String): String = cache.getCache(keywordPreffiex + keywords)

  def putCatagoryByCache(keywords: String, value: String) = cache.putCache(keywordPreffiex + keywords, value)

  override def recommendMostLikeCatagoryIdByKeywords(keywords: String): String = {

    //first get from cache
    val cacheC = getCatagoryByCache(keywords)
    if (cacheC != null) return cacheC
    //top 1 catagoryId4
    val catagoryId4 = sortCatagoeyByKeyword(keywords)
    if (catagoryId4 != null) {
      //put the catagoryId4 into cache
      putCatagoryByCache(keywords, catagoryId4)
    }
    catagoryId4
  }

  def sortCatagoeyByKeyword(keywords: String): String = {
    val query: SolrQuery = new SolrQuery
    var q = "sku:*keyword*^20 OR original:*keyword*^1 OR text:keyword^1 OR pinyin:keyword^-0.2"
    q = q.replaceAll("keyword", keywords.trim)
    log.info("keyword:" + keywords.trim)
    query.set("qt", "/select")
    query.setQuery(q)
    query.set("fl", "categoryId4")
    query.set("fq", "platform:P01")
    query.setSort("score", SolrQuery.ORDER.desc)
    query.setRows(10)
    query.setStart(0)
    val response = solrClient.searchByQuery(query, "searchcloud").asInstanceOf[QueryResponse]
    if (response == null) return null
    val result = response.getResults
    if (result == null || result.size() == 0) return null
    val wordCountMap = new mutable.HashMap[String, Int]()
    result.foreach { doc =>
      wordCountMap(doc.getFirstValue("categoryId4").toString.trim) = wordCountMap.get(doc.getFirstValue("categoryId4").toString.trim).getOrElse(0) + 1
    }
    wordCountMap.toList.sortBy(_._2).last._1
  }
}

object SolrRecommendCategory {
  var solrCategory: SolrRecommendCategory = null

  def apply(): SolrRecommendCategory = {
    if (solrCategory == null) solrCategory = new SolrRecommendCategory()
    solrCategory
  }

  def main(args: Array[String]) {
   println(test2)
  }

  def test2():Long = System.currentTimeMillis()

  def test1() =  new SolrRecommendCategory().recommendMostLikeCatagoryIdByKeywords("工具")
}
