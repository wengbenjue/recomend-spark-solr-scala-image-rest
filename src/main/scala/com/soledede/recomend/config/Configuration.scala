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
  lazy val collection = Try(config.getString("solrj.collection")).getOrElse("searchcloud")
  lazy val zkConnectTimeout = Try(config.getInt("solrj.zkConnectTimeout")).getOrElse(60000)
  lazy val zkClientTimeout = Try(config.getInt("solrj.zkClientTimeout")).getOrElse(60000)
}
