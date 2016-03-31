package com.soledede.recomend.service.impl

import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.cache.KVCache
import com.soledede.recomend.config.Configuration
import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.service.RecommendService
import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  * Created by soledede on 16/2/25.
  */
class SolrRecommendMoreLikeThis private extends RecommendService with SLF4JLogging with Configuration{

  val solrClient = SolrClient()

  val separator = "_$$_"

  val cache = KVCache()


  /**
    *
    * recommend by document id
    * @param docId
    * @param number
    * @return
    */
  override def recommendByDocId(docId: String, number: Int): Seq[RecommendResult] = {
    val q = s"(sku:$docId OR id:$docId)"
    docIdAndCatagoryAndBrandRecommend(docId, number, q,null)
  }

  /**
    *
    * @param catagoryId
    * @param number
    * @return
    */
  override def recommendByCatagoryId(catagoryId: String, number: Int): Seq[RecommendResult] = {
   // val q = "*:*"
    val fq = s"(categoryId1:$catagoryId OR categoryId2:$catagoryId OR categoryId3:$catagoryId OR categoryId4:$catagoryId)"
   val q = s"(categoryId1:$catagoryId OR categoryId2:$catagoryId OR categoryId3:$catagoryId OR categoryId4:$catagoryId)"
    docIdAndCatagoryAndBrandRecommend(catagoryId, number,q,fq)
  }


  /**
    *
    * @param brandId
    * @param number
    * @return
    */
  override def recommendByBrandId(brandId: String, number: Int): Seq[RecommendResult] = {
    val fq = s"brandId:$brandId"
    //val q = "*:*"
    val q = s"brandId:$brandId"
    docIdAndCatagoryAndBrandRecommend(brandId, number, q,fq)
  }

  private def docIdAndCatagoryAndBrandRecommend(id: String, number: Int,  q: String,fq: String): Seq[RecommendResult] = {
    if (id != null && !id.equalsIgnoreCase("")) {

      val cacheRecommendResultList = getFromCache(id, number) //get form cache
      if (cacheRecommendResultList != null) cacheRecommendResultList
      else {
        val fl = "item:sku,weight:score"

        val recommendResutList = getMoreLikeThisRecomendResult(q,fq,fl, number)

        if (recommendResutList != null) {
          putToCache(id, number, recommendResutList)
          recommendResutList

        } else null

      }

    } else null
  }

  private def putToCache(id: String, number: Int, resultItem: Seq[RecommendResult]) = {
    cache.put(id + separator + number, resultItem)
  }

  private def getFromCache(id: String, number: Int): scala.Seq[RecommendResult] = {
    cache.get(id + separator + number)
  }

  private def getMoreLikeThisRecomendResult(q: String, fq: String, fl: String, number: Int): ListBuffer[RecommendResult] = {
    val query = new SolrQuery()

    query.set("qt", "/mlt")
    query.setQuery(q)
    if (fq != null && !fq.trim.equalsIgnoreCase("") && !fq.trim.equalsIgnoreCase("null"))
      query.setFilterQueries(fq)
    query.setFields(fl)
    query.setSort("score", SolrQuery.ORDER.desc)

    query.setStart(0)
    var rows = 10
    if (number > 0) rows = number
    query.setRows(number)

    val r = solrClient.searchByQuery(query, productCollection)

    if (r != null) {

      val response = r.asInstanceOf[QueryResponse]

      val result = getSearchResult(response)

      if (result != null) {
        val listRecommend = new ListBuffer[RecommendResult]()
        result.foreach { doc =>
          val item = doc.get("item").toString
          val weight = doc.get("weight").toString.toDouble
          listRecommend += RecommendResult(item, weight)
        }

        if (listRecommend.size > 0) listRecommend
        else null

      } else null


    } else null
  }


  /**
    *
    * get response Result
    *
    * @param result
    * @return
    */
  private def getSearchResult(result: QueryResponse): java.util.List[java.util.Map[java.lang.String, Object]] = {
    val resultList: java.util.List[java.util.Map[java.lang.String, Object]] = new java.util.ArrayList[java.util.Map[java.lang.String, Object]]() //search result
    //get Result
    if (result != null) {
      val response = result.getResults
      if (response != null) {
        response.foreach { doc =>
          val resultMap: java.util.Map[java.lang.String, Object] = new java.util.HashMap[java.lang.String, Object]()
          val fields = doc.getFieldNames
          fields.foreach { fieldName =>
            resultMap.put(fieldName, doc.getFieldValue(fieldName))
          }
          if (!resultMap.isEmpty)
            resultList.add(resultMap)
        }
      }
    }
    resultList
  }
}

object SolrRecommendMoreLikeThis {
  var solrMoreLikeThis: SolrRecommendMoreLikeThis = null

  def apply(): SolrRecommendMoreLikeThis = {
    if (solrMoreLikeThis == null) solrMoreLikeThis = new SolrRecommendMoreLikeThis()
    solrMoreLikeThis
  }
}

object testSolrRecommendMoreLikeThis{
  def main(args: Array[String]) {
   val moreLikeThis = SolrRecommendMoreLikeThis()
   val result = moreLikeThis.recommendByBrandId("2",18)
    println(result)
  }
}