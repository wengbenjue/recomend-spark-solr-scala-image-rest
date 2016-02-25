package com.soledede.recomend.service.impl


import com.soledede.recomend.clock.{Timing, SystemTimerClock}
import com.soledede.recomend.entity.RecommendResult
import com.soledede.recomend.service.RecommendService
import com.soledede.recomend.solr.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrDocumentList}
import org.apache.solr.common.util.SimpleOrderedMap
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._
import com.soledede.recomend.cache.KVCache

/**
  * Created by soledede on 2015/12/15.
  */
class SolrRecommendCF private extends RecommendService {
  val solrClient = SolrClient()

  val separator = "_$$_"

  val cache = KVCache()

  val period = 1000 * 60 * 60 * 18
  //val period = 3000
  val timer = new Timing(new SystemTimerClock(), period, asyncTopN2Cache, "asyncTop2CACHE")
  timer.start()


  private def asyncTopN2Cache(): Unit = {
    val fq = "status:99"
    var jsonFacet = "{categories:{type:terms,field:docId,limit:80,sort:{weights:desc},facet:{weights:\"sum(weight)\"}}}"
    jsonFacet = jsonFacet.replaceAll(":", "\\:")
    val topNItems = groupBucket(null, fq, jsonFacet)
    if(topNItems !=null) {
      val recommendResult = topNItems.map { kv =>
        val docId = kv.get("val").toString.trim
        val weight = kv.get("weights").asInstanceOf[Double]
        RecommendResult(docId, weight)
      }
      SolrRecommendCF.lock.synchronized {
        SolrRecommendCF.topNItemsList = recommendResult
      }
    }
  }

  //find the history list of current user
  private def searchDocsWeightByUserId(userId: String): SolrDocumentList = {
    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    query.setQuery(s"userId:$userId")
    query.set("fl", "docId,weight")
    query.setSort("weight", SolrQuery.ORDER.desc)
    //query.set("wt", "json")
    query.setRows(20)
    query.setStart(0)
    val response = solrClient.searchByQuery(query, "solritemcf").asInstanceOf[QueryResponse]
    if (response == null) return null
    val result = response.getResults
    result
  }

  //search k-neighbour of userid by docs that boosted
  private def searchUserKNeighbourByDocsBoost(userId: String, docWtList: SolrDocumentList): java.util.List[SimpleOrderedMap[java.lang.Object]] = {
    // search k-neighbour of userid by docs that boosted
    val docsSize = docWtList.size()

    val query = new StringBuilder
    query ++= "docId:("
    var count = 0
    docWtList.foreach { solrDoc =>
      count += 1
      val docId = solrDoc.getFieldValue("docId").toString
      val weight = solrDoc.getFieldValue("weight").toString
      query ++= docId + "^" + weight
      if (docsSize != count)
        query ++= " OR "
    }
    query ++= ")"
    val q = query.toString()
    val fq = "-userId:" + userId
    var jsonFacet = "{categories:{type:terms,field:userId,limit:20,sort:{weights:desc},facet:{weights:\"sum(weight)\"}}}"
    jsonFacet = jsonFacet.replaceAll(":", "\\:")
    groupBucket(q, fq, jsonFacet)
  }

  //search recommend docs by users that have boosted
  def searchDocsByNearstUsers(kNearstUsers: java.util.List[SimpleOrderedMap[java.lang.Object]], userId: String, recomendNum: Int) = {
    val uSize = kNearstUsers.size()
    val query = new StringBuilder
    query ++= "userId:("
    var count = 0
    for (kv <- kNearstUsers) {
      count += 1
      val userId = kv.get("val").toString.trim
      val weight = kv.get("weights").toString.trim
      query ++= userId + "^" + weight
      if (uSize != count)
        query ++= " OR "
    }
    query ++= ")"
    val q = query.toString()
    val fq = "-userId:" + userId + " AND status:99"
    var jsonFacet = "{categories:{type:terms,field:docId,limit:" + recomendNum + ",sort:{weights:desc},facet:{weights:\"sum(weight)\"}}}"
    jsonFacet = jsonFacet.replaceAll(":", "\\:")
    groupBucket(q, fq, jsonFacet)
  }

  //recommend from popularity
  def recommendFromTopN(number: Int): Seq[RecommendResult] = {
    if (SolrRecommendCF.topNItemsList == null) {
      SolrRecommendCF.lock.synchronized {
        asyncTopN2Cache()
        SolrRecommendCF.topNItemsList.take(number)
      }
    } else SolrRecommendCF.topNItemsList.take(number)
  }

  //recommend from popularity
  def topNPlusRecommend(recommendResult: scala.collection.mutable.Buffer[RecommendResult], number: Int): Seq[RecommendResult] = {
    var result = recommendResult
    if (SolrRecommendCF.topNItemsList == null) {
      SolrRecommendCF.lock.synchronized {
        result ++= SolrRecommendCF.topNItemsList.take(number)
      }
    } else result ++= SolrRecommendCF.topNItemsList.take(number)
    result
  }

  //filter docs current user purchased
  def filterRecommendFesultForUser(docWtList: SolrDocumentList, recommendAllDocs: java.util.List[SimpleOrderedMap[Object]], number: Int): Seq[RecommendResult] = {
    val result = recommendAllDocs.filter { kv =>
      val docId = kv.get("val").toString.trim
      var flag = false
      breakable {
        docWtList.foreach { solrDoc =>
          val id = solrDoc.getFieldValue("docId").toString.trim
          if (docId.equalsIgnoreCase(id)) {
            flag = true
            break
          }
        }
      }
      if (flag) false
      else true
    }
    val recommendResult = result.map { kv =>
      val docId = kv.get("val").toString.trim
      val weight = kv.get("weights").asInstanceOf[Double]
      RecommendResult(docId, weight)
    }
    val rL = recommendResult.size
    if (rL < number) topNPlusRecommend(recommendResult, number - rL)
    else if (rL > number) recommendResult.take(number)
    else recommendResult
  }

  def putToCache(userId: String, number: Int, resultItem: Seq[RecommendResult]) = {
    cache.put(userId + separator + number, resultItem)
  }

  def getFromCache(userId: String, number: Int): scala.Seq[RecommendResult] = {
    cache.get(userId + separator + number)
  }

  override def recommendByUserId(userId: String, number: Int): Seq[RecommendResult] = {
    //get result from cache if there is result
    val itemResult = getFromCache(userId: String, number: Int)
    if (itemResult != null && itemResult.size > 0) return itemResult
    // search docs by userid
    val docWtList = searchDocsWeightByUserId(userId)
    if (docWtList == null || docWtList.size() == 0) return recommendFromTopN(number) //recommend from popularity
    val dSize = docWtList.size()
    //search k-neighbour of userid by docs that boosted
    val kNearstUsers = searchUserKNeighbourByDocsBoost(userId, docWtList)
    if (kNearstUsers == null || kNearstUsers.size() == 0) {
      //put userid_number to cache
      val resultItem = recommendFromTopN(number)
      putToCache(userId, number, resultItem)
      return resultItem //recommend from popularity
    }

    // search recommend docs by users that boosted but not(docs that cuurent userid purchased)
    //search recommend docs by users that have boosted
    val recommendAllDocs = searchDocsByNearstUsers(kNearstUsers, userId, number + dSize)
    if (recommendAllDocs == null || recommendAllDocs.size() == 0) {
      //put userid_number to cache
      val resultItem = recommendFromTopN(number)
      putToCache(userId, number, resultItem)
      return resultItem //recommend from popularity
    }
    //solrClient.close()

    //filter docs current user purchased
    val resultItem = filterRecommendFesultForUser(docWtList, recommendAllDocs, number)
    //put userid_number to cache
    putToCache(userId, number, resultItem)
    resultItem
  }


  private def groupBucket(q: String, fq: String, json_facet: String): java.util.List[SimpleOrderedMap[java.lang.Object]] = {
    if (json_facet == null) return null
    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    if (q != null && !q.trim.equalsIgnoreCase("")) query.setQuery(q) else query.setQuery("*:*")
    if (fq != null && !fq.trim.equalsIgnoreCase(""))
      query.set("fq", fq)
    query.setParam("json.facet", json_facet)
    query.setRows(0)
    query.setStart(0)
    val rt = solrClient.searchByQuery(query, "solritemcf")
    if (rt == null) return null
    else {
      val r = rt.asInstanceOf[QueryResponse]
      val fMap = r.getResponse
      val facetsMap = fMap.get("facets").asInstanceOf[SimpleOrderedMap[SimpleOrderedMap[java.util.List[SimpleOrderedMap[java.lang.Object]]]]]
      val catagoryMap = facetsMap.get("categories")
      val bucketList = catagoryMap.get("buckets")
      bucketList
    }
  }

  def testSearchByQuery() = {
    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    //query.add("q", "userId:39")
    //query.addField("docId,weight")
    query.setQuery("*:*")
    query.set("fq", "status:99")
    var json_param = "{categories:{type:terms,field:docId,limit:20,sort:{count:desc}}}"
    json_param = json_param.replaceAll(":", "\\:")
    //json_param = json_param.replaceAll("{","\\{")
    //json_param = json_param.replaceAll("}","\\}")
    println(json_param)
    query.setParam("json.facet", json_param)
    query.setRows(0)
    query.setStart(0)
    println(query.toString)
    val r = solrClient.searchByQuery(query, "solritemcf").asInstanceOf[QueryResponse]
    solrClient.close()
    val fMap = r.getResponse
    val facetsMap = fMap.get("facets").asInstanceOf[SimpleOrderedMap[SimpleOrderedMap[java.util.List[SimpleOrderedMap[java.lang.Long]]]]]
    val catagoryMap = facetsMap.get("categories")
    val bucketList = catagoryMap.get("buckets")
    for (kv <- bucketList) {
      println(kv.get("val"))
      println(kv.get("count"))
    }


    /*fMap.foreach { m =>
      println(m.getKey + "=" + m.getValue)
    }*/
    // r.getResults.foreach(println)
  }

  def testSearchByQuery1() = {
    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    //query.add("q", "userId:39")
    //query.addField("docId,weight")
    //var testQ = "docId:(L222463^1.1+OR+L222464^1.1+OR+L222466^1.1+OR+L251972^1.1+OR+MAD458^1.1+OR+MAP281^1.1+OR+MBC073^1.1+OR+L5070012^1.1+OR+MAD625^1.1+OR+MAD627^1.1+OR+MAL252^1.1+OR+MBC068^1.1+OR+SP-15415^1.1)"
    var testQ = "userId:(0^10.629999999999999 OR 9790^5.13 OR 13832^4.68 OR 10994^4.17 OR 13875^4.17 OR 15462^3.88 OR 11947^3.3000000000000003 OR 12299^3.3000000000000003 OR 12444^3.3000000000000003 OR 13687^3.3000000000000003 OR 7115^3.3000000000000003 OR 9111^3.3000000000000003 OR 10858^2.78 OR 11246^2.2 OR 11868^2.2 OR 12619^2.2 OR 12642^2.2 OR 5840^2.2 OR 13780^1.79 OR 16757^1.79)"
    testQ = testQ.replaceAll("\\+", " ")
    query.setQuery(testQ)
    var json_param = "{categories:{type:terms,field:docId,limit:16,sort:{weights:desc},facet:{weights:\"sum(weight)\"}}}"
    json_param = json_param.replaceAll(":", "\\:")
    //json_param = json_param.replaceAll("{","\\{")
    //json_param = json_param.replaceAll("}","\\}")
    println(json_param)
    query.setParam("json.facet", json_param)
    query.setRows(0)
    query.setStart(0)
    println(query.toString)
    val r = solrClient.searchByQuery(query, "solritemcf").asInstanceOf[QueryResponse]
    solrClient.close()
    val fMap = r.getResponse
    val facetsMap = fMap.get("facets").asInstanceOf[SimpleOrderedMap[SimpleOrderedMap[java.util.List[SimpleOrderedMap[java.lang.Long]]]]]
    val catagoryMap = facetsMap.get("categories")
    val bucketList = catagoryMap.get("buckets")
    for (kv <- bucketList) {
      println(kv.get("val"))
      //println(kv.get("count"))
    }


    /*fMap.foreach { m =>
      println(m.getKey + "=" + m.getValue)
    }*/
    // r.getResults.foreach(println)
  }


}

object SolrRecommendCF {
  val lock = new Object
  var solrCf: SolrRecommendCF = null

  def apply(): SolrRecommendCF = {
    if (solrCf == null) solrCf = new SolrRecommendCF()
    solrCf
  }

  var topNItemsList: Seq[RecommendResult] = null

  def main(args: Array[String]): Unit = {
    test2()
  }

  def test2() = {
    val s = new SolrRecommendCF()
    println("$$$$$$$$$$$$$$$$$$$$$$$$" + s.recommendByUserId("23432", 54))
    //s.solrClient.close()
    //Thread.sleep(1000)
    //s.solrClient.connect()
    //Thread.sleep(5000)
    println("############" + s.recommendByUserId("23432", 54))
    println(s.recommendByUserId("23433", 2))
    s.solrClient.close()
  }

  def test1() = {
    //new SolrRecommendCF().asyncTopN2Cache()
    val solrCf = new SolrRecommendCF()
    /*  Thread.sleep(6 * 1000)
      topNItemsList
      print("test")*/
    val buffer = ListBuffer[RecommendResult]()
    buffer += RecommendResult("item1", 23)
    buffer += RecommendResult("item2", 24)
    solrCf.cache.put("test123", buffer)
    //Thread.sleep(9000)
    println(solrCf.cache.get("test123"))
  }


}
