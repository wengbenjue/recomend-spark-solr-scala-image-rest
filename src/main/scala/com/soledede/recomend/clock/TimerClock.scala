package com.soledede.recomend.clock

/**
  * Created by soledede on 2015/12/18.
  */
trait TimerClock {
  def currentTime(): Long

  def waitToTime(targetTime: Long): Long
}

class SystemTimerClock() extends TimerClock {
  val minSleepime = 25L

  def currentTime(): Long = {
    System.currentTimeMillis()
  }

  def waitToTime(targetTime: Long): Long = {
    var currentTime = 0L
    currentTime = System.currentTimeMillis()

    var waitTime = targetTime - currentTime
    if (waitTime <= 0) {
      return currentTime
    }

    val howSleepTime = math.max(waitTime / 10.0, minSleepime).toLong

    while (true) {
      currentTime = System.currentTimeMillis()
      waitTime = targetTime - currentTime
      if (waitTime <= 0) {
        return currentTime
      }
      val sleepTime = math.min(waitTime, howSleepTime)
      Thread.sleep(sleepTime)
    }
    -1
  }
}