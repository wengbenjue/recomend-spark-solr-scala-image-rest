package com.soledede.recomend.solr.impl

import java.util

import com.soledede.recomend.config.Configuration
import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.impl.{XMLResponseParser, HttpSolrClient, HttpSolrServer}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.params.SolrParams
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/16.
  */
class HttpSolrQuery extends SolrClient with Configuration{
  //val httpPrefix: String = "http://image/"
  val httpPrefix: String = "http://" + imageHost + ":" + imagePort + "/"


  override def searchByUrl(url: String): AnyRef = {

    null
  }

  override def searchByQuery[T: ClassTag](baseUrl: String, query: SolrQuery, collection: String, requestHandler: String): Seq[T] = {

    val server = HttpSolrQuery.singletonHttpSolrClient(baseUrl)
    val response: QueryResponse = server.query(collection, query)
    var listString: List[String] = null
    if (response != null) {
      var docs = response.getResponse.get("docs")
      if (docs != null) {
        val mapDocs = docs.asInstanceOf[util.ArrayList[java.util.LinkedHashMap[String, String]]]

        listString = mapDocs.map { doc =>
          val filePath = doc.get("id").toString
          val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
          httpPrefix + fileName
        }.toList
      }

      //println(listString)
    }
    listString.asInstanceOf[Seq[T]]
  }
}

object HttpSolrQuery {
  var server: HttpSolrClient = null
  var serverMap: mutable.Map[String, HttpSolrClient] = new mutable.HashMap[String, HttpSolrClient]()
  var query: HttpSolrQuery = null

  def apply(): HttpSolrQuery = {
    if (query == null) query = new HttpSolrQuery
    query
  }

  def singletonHttpSolrClient(url: String): HttpSolrClient = {
    if (!serverMap.contains(url.trim)) {
      val server = new HttpSolrClient(url)
      //server.setBaseURL();
      //server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
      server.setConnectionTimeout(60*1000) // 1 minute to establish TCP
      // Setting the XML response parser is only required for cross
      // version compatibility and only when one side is 1.4.1 or
      // earlier and the other side is 3.1 or later.
      //server.setParser(new XMLResponseParser()); // binary parser is used by default
      // The following settings are provided here for completeness.
      // They will not normally be required, and should only be used
      // after consulting javadocs to know whether they are truly required.
      server.setSoTimeout(4000) // socket read timeout
      server.setDefaultMaxConnectionsPerHost(100)
      server.setMaxTotalConnections(100)
      // server.setFollowRedirects(false); // defaults to false
      // allowCompression defaults to false.
      // Server side must support gzip or deflate for this to have any effect.
      //server.setAllowCompression(true);
      serverMap(url.trim) = server
    }
    serverMap(url.trim)
  }


  def main(args: Array[String]) {
    val url = "http://image.search.com:10000/solr/"
    val query: SolrQuery = new SolrQuery();
    query.setRequestHandler("/image")
    query.set("url", "http://image/Corbis-42-67718653.jpg")
    query.setFields("id")
    query.setStart(0)
    query.setRows(10)
    HttpSolrQuery().searchByQuery(url, query)

  }
}