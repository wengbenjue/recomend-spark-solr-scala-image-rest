package com.soledede.recomend.clock

import akka.event.slf4j.SLF4JLogging
import com.soledede.recomend.util.Util

/**
  * Created by soledede on 2015/12/18.
  */
class Timing(clock: TimerClock, var period: Long, callback: () => Unit, name: String) extends SLF4JLogging {
  private var thread = new Thread("RecurringTimer - " + name) {
    setDaemon(true)

    override def run() {
      ring
    }
  }

  @volatile private var prevTime = -1L
  @volatile private var nextTime = -1L
  @volatile private var stopped = false


  def getStartTime(): Long = {
    (math.floor(clock.currentTime.toDouble / period) + 1).toLong * period
  }


  def getRestartTime(originalStartTime: Long): Long = {
    val gap = clock.currentTime - originalStartTime
    (math.floor(gap.toDouble / period).toLong + 1) * period + originalStartTime
  }

  def start(startTime: Long): Long = synchronized {
    nextTime = startTime
    thread.start()
    log.info("Started timer for " + name)
    nextTime
  }


  def start(): Long = {
    start(getStartTime())
  }


  def stop(interruptTimer: Boolean): Long = synchronized {
    if (!stopped) {
      stopped = true
      if (interruptTimer) {
        thread.interrupt()
      }
      thread.join()
      log.info("Stopped timer for " + name + " after time " + prevTime)
    }
    prevTime
  }

  def resetNextTime(nextTime: Long) {
    this.nextTime = nextTime
    log.info("reset timer for " + name + " next time " + Util.convertDateFormat(nextTime))
  }

  def restart() {
    stop(true)
    thread = null
    thread = new Thread("RecurringTimer - " + name) {
      setDaemon(true)

      override def run() {
        ring
      }
    }
    start()
    stopped = false
  }

  /**
    * Timing call the callback every interval.
    */
  private def ring() {
    try {
      while (!stopped) {
        clock.waitToTime(nextTime)
        callback()
        prevTime = nextTime
        nextTime += period
        log.debug("Callback for " + name + " called at time " + Util.convertDateFormat(prevTime))
      }
    } catch {
      case e: InterruptedException =>
    }
  }

}
object Timing {

  def main(args: Array[String]) {
    val period = 1000
    val timer = new  Timing(new SystemTimerClock(), period, test, "Test")
    timer.start()
    Thread.sleep(30 * 1000)
    timer.stop(true)
  }

  def test():Unit ={
    println("进来了，当前时间："+System.currentTimeMillis())

  }
}