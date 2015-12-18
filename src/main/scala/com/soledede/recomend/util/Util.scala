package com.soledede.recomend.util

import java.text.SimpleDateFormat

/**
  * Created by soledede on 2015/12/18.
  */
object Util {

  def convertDateFormat(time: Long, format: String = "yyyy-MM-dd HH:mm:ss SSS"): String = {
    val date = new java.util.Date(time)
    val formatter = new SimpleDateFormat(format)
    formatter.format(date)
  }

}
