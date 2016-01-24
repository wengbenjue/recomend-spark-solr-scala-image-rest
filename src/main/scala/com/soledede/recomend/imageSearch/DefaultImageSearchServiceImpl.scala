package com.soledede.recomend.imageSearch

import com.soledede.recomend.cache.KVCache
import com.soledede.recomend.config.Configuration
import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.rest.VMsg
import com.soledede.recomend.solr.impl.HttpSolrQuery
import org.apache.solr.client.solrj.SolrQuery

/**
  * Created by soledede on 16/1/15.
  */
class DefaultImageSearchServiceImpl extends ImageSearchService with Configuration {
  val url = "http://" + solrHost + ":" + solrPort + "/solr/"
  val searchUrlPrefix = "http://" + imageHost + ":" + imagePort + "/"

  val cache = KVCache()

  override def search(fileName: String, start: Int, size: Int): Seq[String] = {

    val resultCache = getFromCache(fileName)
    if (resultCache != null && resultCache.size > 0) return resultCache

    val query: SolrQuery = new SolrQuery();
    query.setRequestHandler("/image")
    query.set("url", searchUrlPrefix + fileName.trim)
    query.setFields("id")
    query.setParam("accuracy", "0.99")
    query.setParam("candidates", "85000")
    query.setStart(start)
    query.setRows(size)
    val rR = HttpSolrQuery().searchByQuery[Seq[String]](url, query)
    var r:Seq[String] = null
      if(rR != null)
      r = rR.asInstanceOf[Seq[String]]
    if (r != null && r.size > 0) putToCache(fileName, r)
    r
  }

  def putToCache(fileName: String, resultVMsg: Seq[String]) = {
    cache.putStringList(fileName, resultVMsg)
  }

  def getFromCache(fileName: String): Seq[String] = {
    cache.getStringList(fileName)
  }
}

object DefaultImageSearchServiceImpl {
  var searchService: DefaultImageSearchServiceImpl = null

  def apply(): DefaultImageSearchServiceImpl = {
    if (searchService == null) searchService = new DefaultImageSearchServiceImpl
    searchService
  }

}
