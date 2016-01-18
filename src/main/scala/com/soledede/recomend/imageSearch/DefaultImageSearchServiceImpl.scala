package com.soledede.recomend.imageSearch

import com.soledede.recomend.config.Configuration
import com.soledede.recomend.solr.impl.HttpSolrQuery
import org.apache.solr.client.solrj.SolrQuery

/**
  * Created by soledede on 16/1/15.
  */
class DefaultImageSearchServiceImpl extends ImageSearchService with Configuration {
  val url = "http://" + solrHost + ":" + solrPort + "/solr/"
  val searchUrlPrefix = "http://" + imageHost + ":" + imagePort + "/"

  override def search(fileName: String, start: Int, size: Int): Seq[String] = {
    val query: SolrQuery = new SolrQuery();
    query.setRequestHandler("/image")
    query.set("url", searchUrlPrefix + fileName.trim)
    query.setFields("id")
    query.setStart(start)
    query.setRows(size)
    HttpSolrQuery().searchByQuery[Seq[String]](url, query).asInstanceOf[Seq[String]]
  }
}

object DefaultImageSearchServiceImpl {
  var searchService: DefaultImageSearchServiceImpl = null

  def apply(): DefaultImageSearchServiceImpl = {
    if (searchService == null) searchService = new DefaultImageSearchServiceImpl
    searchService
  }

}
