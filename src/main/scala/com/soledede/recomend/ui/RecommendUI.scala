package com.soledede.recomend.ui

import com.soledede.recomend.entity.User

/**
  * Created by soledede on 2015/12/15.
  */
trait RecommendUI {

  def recommendByUser(user: User): Seq[String] = null

  def recommendByUserId(userId: Int): Seq[String] = null

  def recommendByCatagoryByUser(user: User): Seq[String] = null

}
