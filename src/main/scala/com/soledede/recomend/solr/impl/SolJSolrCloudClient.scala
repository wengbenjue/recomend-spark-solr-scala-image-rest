package com.soledede.recomend.solr.impl

import com.soledede.recomend.config.Configuration
import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.{BinaryRequestWriter, CloudSolrClient}
import org.apache.solr.client.solrj.response.QueryResponse

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/15.
  */
class SolJSolrCloudClient private extends SolrClient {
  val server: CloudSolrClient = SolJSolrCloudClient.singleCloudInstance()

  override def searchByQuery[T: ClassTag](query: T, collection: String = "searchcloud"): AnyRef = {
    if (server == null) server.connect()
    var response: QueryResponse = null
    try {
      response = server.query(collection, query.asInstanceOf[SolrQuery])
    } catch {
      case e: Exception =>
        e.printStackTrace()
        server.close()
      //TODO Log
    }
    response
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
        }
      }
    }
    server
  }


  def connect() = {
    lockSearch.synchronized {
      if (server == null) {
        server.connect
      }
    }
  }

  def close() = {
    lockSearch.synchronized {
      if (server != null)
        server.close
    }
  }

}