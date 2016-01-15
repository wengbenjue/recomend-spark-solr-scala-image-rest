package com.soledede.recomend.solr

import com.soledede.recomend.solr.impl.SolJSolrCloudClient
import org.apache.solr.client.solrj.SolrQuery

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/15.
  */
trait SolrClient {

  def searchByQuery[T: ClassTag](query: T, collection: String = "searchcloud"): AnyRef = null

  def searchByQuery[T: ClassTag](baseUrl: String,query: SolrQuery, collection: String , requestHandler: String): Seq[T] = null

  def searchByQuery[T: ClassTag](baseUrl: String,query: SolrQuery, collection: String = "imagesearch"): Seq[T] = searchByQuery(baseUrl,query,collection,null)

  def searchByUrl(url: String): AnyRef = null

  def close(): Unit = {}

  def connect(): Unit = {}
}

object SolrClient {

  def apply(cType: String = "solrJSolrCloud"): SolrClient = {
    cType match {
      case "solrJSolrCloud" => SolJSolrCloudClient()
      case _ => null
    }
  }
}
