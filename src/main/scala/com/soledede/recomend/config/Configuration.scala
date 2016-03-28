package com.soledede.recomend.config

import com.soledede.recomend.solr.impl.SolJSolrCloudClient._
import com.typesafe.config.ConfigFactory
import org.apache.solr.client.solrj.impl.{BinaryRequestWriter, CloudSolrClient}
import util.Try

/**
  * Holds service configuration settings.
  */
trait Configuration {

  /**
    * Application config object.
    */
  val config = ConfigFactory.load()

  /** Host name/address to start service on. */
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")

  /** Port to start service on. */
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8088)

  //solr
  lazy val zkHostString = Try(config.getString("solrj.zk")).getOrElse("solr1:3213,solr2:3213,solr3:3213/solr")
  lazy val collection = Try(config.getString("solrj.collection")).getOrElse("solritemcf")
  lazy val zkConnectTimeout = Try(config.getInt("solrj.zkConnectTimeout")).getOrElse(60000)
  lazy val zkClientTimeout = Try(config.getInt("solrj.zkClientTimeout")).getOrElse(60000)

  lazy val productCollection = Try(config.getString("solrj.productCollection")).getOrElse("mergescloud_prod")

  //redis
  lazy val redisHost = Try(config.getString("redis.host")).getOrElse("localhost")
  lazy val redisPort = Try(config.getInt("redis.port")).getOrElse(6379)


  //open-off
  lazy val openRecommend = Try(config.getBoolean("recommend.open")).getOrElse(false)


  //image search
  lazy val solrHost = Try(config.getString("imagesearch.solrHost")).getOrElse("localhost")
  lazy val solrPort = Try(config.getInt("imagesearch.solrPort")).getOrElse(10000)

  lazy val imageHost = Try(config.getString("imagesearch.imageHost")).getOrElse("localhost")
  lazy val imagePort = Try(config.getInt("imagesearch.imagePort")).getOrElse(80)
  lazy val fileDir = Try(config.getString("imagesearch.fileDir")).getOrElse("/Users/soledede/Documents/")

}
