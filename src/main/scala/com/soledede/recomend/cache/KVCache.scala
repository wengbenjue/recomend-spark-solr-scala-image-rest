package com.soledede.recomend.cache

/**
  * Created by soledede on 2015/12/18.
  */
trait KVCache {

  def put(key: String,value: Any,expiredTime: Long)

  def get(key: String): Any


}

