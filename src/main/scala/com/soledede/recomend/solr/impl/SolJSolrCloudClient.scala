package com.soledede.recomend.solr.impl

import akka.event.slf4j.{SLF4JLogging}
import com.soledede.recomend.config.Configuration
import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.{BinaryRequestWriter, CloudSolrClient}
import org.apache.solr.client.solrj.response.QueryResponse

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/15.
  */
class SolJSolrCloudClient private extends SolrClient with SLF4JLogging {
  var server: CloudSolrClient = SolJSolrCloudClient.singleCloudInstance()

  override def searchByQuery[T: ClassTag](query: T, collection: String = "searchcloud"): AnyRef = {
    if(server==null) server =  SolJSolrCloudClient.singleCloudInstance()
    var response: QueryResponse = null
    try {
      response = server.query(collection, query.asInstanceOf[SolrQuery])
    } catch {
      case e: Exception =>
        log.error("search error", e.getCause)
        server.close()
      //TODO Log
    }
    response
  }


  override def connect(): Unit = {
    SolJSolrCloudClient.connect()
  }

  override def close(): Unit = {
    SolJSolrCloudClient.close()
  }
}

object SolJSolrCloudClient extends Configuration {

  val lockSearch = new Object
  val lockKwSearch = new Object

  var solrJClient: SolJSolrCloudClient = null

  def apply(): SolrClient = {
    if (solrJClient == null) solrJClient = new SolJSolrCloudClient()
    solrJClient
  }

  var server: CloudSolrClient = null

  def singleCloudInstance(): CloudSolrClient = {
    if (server == null) {
      lockSearch.synchronized {
        if (server == null) {
          server = new CloudSolrClient(zkHostString)
          server.setDefaultCollection(collection)
          server.setZkConnectTimeout(zkConnectTimeout)
          server.setZkClientTimeout(zkClientTimeout)
          server.setRequestWriter(new BinaryRequestWriter())
          server.connect()
        }
      }
    }
    server
  }


  def connect() = {
    lockSearch.synchronized {
      singleCloudInstance()
    }
  }

  def close() = {
    lockSearch.synchronized {
      if (server != null)
        server.close
      server =null
    }
  }

}