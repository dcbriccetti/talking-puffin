package org.talkingpuffin.util

import java.text.NumberFormat

/**
 * Times and logs a function
 */
object TimeLogger {
  val fmt = NumberFormat.getInstance

  def run[T](log: (String) => Unit, msg: String, fn: => T): T = {
    log(msg)
    val startTime = System.currentTimeMillis
    val result = fn
    log("Done in " + fmt.format(System.currentTimeMillis - startTime) + " ms")
    result
  }
}
