package com.soledede.recomend.dao

import java.sql._

import com.soledede.recomend.config.Configuration
import com.soledede.recomend.domain.{FailureType, Failure}
import com.soledede.recomend.util.HbaseTool
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.mapreduce.TableInputFormat

import scala.util.parsing.json.JSONArray

/**
  * Provides DAL for Customer entities for MySQL database.
  */
object HbaseRecommendModel {
  private var hb = HbaseTool
  var hbm: HbaseRecommendModel = null

  def apply() = {
    if (hbm == null) hbm = new HbaseRecommendModel()
    hbm
  }

}

class HbaseRecommendModel private extends Configuration {

  {
    //lond the config of Hbase，create Table recomend
    var confHbase = HBaseConfiguration.create()
    confHbase.set("hbase.zookeeper.property.clientPort", "2181")
    confHbase.set("hbase.zookeeper.quorum", "spark1.soledede.com,spark2.soledede.com,spark3.soledede.com")
    confHbase.set("hbase.master", "spark1.soledede.com:60000")
    confHbase.addResource("/opt/cloudera/parcels/CDH/lib/hbase/conf/hbase-site.xml")
    confHbase.set(TableInputFormat.INPUT_TABLE, "recomend")
    HbaseRecommendModel.hb.setConf(confHbase)
  }

  protected def exError(e: Exception) =
    Failure("%d: %s".format(-1, e.getMessage), FailureType.DatabaseFailure)

  /**
    *
    * @param id
    * @return
    */
  def get(id: String): Either[Failure, JSONArray] = {
    try {
      println("Match==================" + id)
      val list = HbaseRecommendModel.hb.getSingleValue("recomend", id, "top", "resid").list
      var resIdList = for (resNum <- list; if (resNum != null)) yield HbaseRecommendModel.hb.getMapSingleValue("mapping", resNum.toString, "res", "resid")
      resIdList = resIdList.filter(resid => resid != null)
      //ModuleDAO.hb.getMapSingleValue("mapping",id,"res","userid")
      //list.foreach(println)
      //Left(notFoundError(id))
      Right(JSONArray(resIdList))
    } catch {
      case e: Exception =>
        Left(exError(e))
    }
  }

}