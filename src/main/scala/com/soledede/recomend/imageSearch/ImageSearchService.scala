package com.soledede.recomend.imageSearch

/**
  * Created by soledede on 16/1/15.
  *
  * search for similarity images
  * eg: search by url with solr
  */
trait ImageSearchService {

  def search(fileName: String, start: Int, size: Int): Seq[String]
}

object ImageSearchService {

  def apply(imageServiceType: String = "default"): ImageSearchService = {
    imageServiceType match {
      case "default" => DefaultImageSearchServiceImpl()
      case _ => null
    }
  }
}