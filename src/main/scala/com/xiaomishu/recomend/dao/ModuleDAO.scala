package com.sysgears.recomend.dao

import com.xiaomishu.recomend.config.Configuration
import com.xiaomishu.recomend.domain._
import java.sql._
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import play.api.libs.json.{JsValue, Json}

import scala.Some
import scala.List
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.driver.MySQLDriver.simple._
import scala.util.parsing.json.{JSONArray, JSON}
import slick.jdbc.meta.MTable
import com.xiaomishu.recomend.util.HbaseTool
/**
 * Provides DAL for Customer entities for MySQL database.
 */
object ModuleDAO{
  private  var hb = HbaseTool
}

class ModuleDAO extends Configuration {

  {
    //lond the config of Hbase，create Table recomend
    var confHbase = HBaseConfiguration.create()
    confHbase.set("hbase.zookeeper.property.clientPort", "2181")
    confHbase.set("hbase.zookeeper.quorum", "h4.xiaomishu.com,h2.xiaomishu.com,h6.xiaomishu.com,h5.xiaomishu.com,h7.xiaomishu.com,h1.xiaomishu.com,h3.xiaomishu.com,h10.xiaomishu.com,h9.xiaomishu.com,h8.xiaomishu.com")
    confHbase.set("hbase.master", "h1.xiaomishu.com:60000")
    confHbase.addResource("/opt/cloudera/parcels/CDH/lib/hbase/conf/hbase-site.xml")
    confHbase.set(TableInputFormat.INPUT_TABLE, "recomend")
    ModuleDAO.hb.setConf(confHbase)
  }
  // init Database instance
  private val db = Database.forURL(url = "jdbc:mysql://%s:%d/%s".format(dbHost, dbPort, dbName),
    user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

  // create tables if not exist
  db.withSession {
    if (MTable.getTables("customers").list().isEmpty) {
      Customers.ddl.create
    }
  }

  /**
   * Saves customer entity into database.
   *
   * @param customer customer entity to
   * @return saved customer entity
   */
  def create(customer: Customer): Either[Failure, Customer] = {
    try {
      val id = db.withSession {
        Customers returning Customers.id insert customer
      }
      Right(customer.copy(id = Some(id)))
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
   * Updates customer entity with specified one.
   *
   * @param id       id of the customer to update.
   * @param customer updated customer entity
   * @return updated customer entity
   */
  def update(id: Long, customer: Customer): Either[Failure, Customer] = {
    try
      db.withSession {
        Customers.where(_.id === id) update customer.copy(id = Some(id)) match {
          case 0 => Left(notFoundError(id.toString))
          case _ => Right(customer.copy(id = Some(id)))
        }
      }
    catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
   * Deletes customer from database.
   *
   * @param id id of the customer to delete
   * @return deleted customer entity
   */
  def delete(id: Long): Either[Failure, Customer] = {
    try {
      db.withTransaction {
        val query = Customers.where(_.id === id)
        val customers = query.run.asInstanceOf[List[Customer]]
        customers.size match {
          case 0 =>
            Left(notFoundError(id.toString))
          case _ => {
            query.delete
            Right(customers.head)
          }
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
   * Retrieves specific customer from database.
   *
   * @param id id of the customer to retrieve
   * @return customer entity with specified id
   */
  def get(id: String): Either[Failure,JSONArray] = {
    try {
    /**  db.withSession {
        Customers.findById(id).firstOption match {
          case Some(customer: Customer) =>
            Right(customer)
          case _ =>
            Left(notFoundError(id))Ｌｉｓｔ
        }
      }
      **/
      println("Match=================="+id)
     val list = ModuleDAO.hb.getSingleValue("recomend",id,"top","resid").list
      var resIdList =  for(resNum <- list;if(resNum != null)) yield ModuleDAO.hb.getMapSingleValue("mapping",resNum.toString,"res","resid")
      resIdList =  resIdList.filter(resid => resid != null)
      //ModuleDAO.hb.getMapSingleValue("mapping",id,"res","userid")
      //list.foreach(println)
     //Left(notFoundError(id))
     Right(JSONArray(resIdList))
    } catch {
      case e: Exception =>
        Left(exError(e))
    }
  }

  /**
   * Retrieves list of customers with specified parameters from database.
   *
   * @param params search parameters
   * @return list of customers that match given parameters
   */
  def search(params: CustomerSearchParameters): Either[Failure, List[Customer]] = {
    implicit val typeMapper = Customers.dateTypeMapper

    try {
      db.withSession {
        val query = for {
          customer <- Customers if {
          Seq(
            params.firstName.map(customer.firstName is _),
            params.lastName.map(customer.lastName is _),
            params.birthday.map(customer.birthday is _)
          ).flatten match {
            case Nil => ConstColumn.TRUE
            case seq => seq.reduce(_ && _)
          }
        }
        } yield customer

        Right(query.run.toList)
      }
    } catch {
      case e: Exception =>
        Left(exError(e))
    }
  }

  /**
   * Produce database error description.
   *
   * @param e SQL Exception
   * @return database error description
   */
  protected def databaseError(e: SQLException) =
    Failure("%d: %s".format(e.getErrorCode, e.getMessage), FailureType.DatabaseFailure)

  /**
   * Produce customer not found error description.
   *
   * @param  userId of the customer
   * @return not found error description
   */
  protected def notFoundError(userId: String) =
    Failure("Customer with id=%d does not exist".format(userId), FailureType.NotFound)


  protected def exError(e: Exception) =
    Failure("%d: %s".format(-1, e.getMessage), FailureType.DatabaseFailure)



}