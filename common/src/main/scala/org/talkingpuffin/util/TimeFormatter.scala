package org.talkingpuffin.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.Seconds.secondsBetween

/**
 * Time formatter
 */
case class TimeFormatter(time: Long) {
  val days = time / 86400
  val hours = (time / 3600) - (days * 24)
  val minutes = (time / 60) - (days * 1440) - (hours * 60)
  val seconds = time % 60
  
  def colonSeparated: String = {
    val sb = new StringBuilder
    if (days > 0) sb.append(days).append(":")
    if (hours > 0    || sb.length > 0) sb.append(TimeUtil2.twoDigitNum(hours))   .append(":")
    if (minutes > 0  || sb.length > 0) sb.append(TimeUtil2.twoDigitNum(minutes)) .append(":")
    sb.append(TimeUtil2.twoDigitNum(seconds))
    sb.toString
  }

  def longForm: String = {
    val sb = new StringBuilder
    addPart(sb, days,    "day")
    addPart(sb, hours,   "hour")
    addPart(sb, minutes, "min")
    addPart(sb, seconds, "sec")

  def addPart(sb: StringBuilder, value: Long, name: String): Unit = {
      if (value > 0) {
        if (sb.length > 0) sb.append(' ')
        sb.append(value).append(' ').append(name)
        if (value > 1) sb.append('s')
      }
    }
    
    sb.toString
  }
}

object TimeUtil2 {
  private val fmt      = DateTimeFormat.forPattern("M/dd H:mm")
  private val fmtNoDay = DateTimeFormat.forPattern("H:mm")

  def formatAge(date: DateTime, showAsAge: Boolean): String =
    if (showAsAge)
      new TimeFormatter(toAge(date)).colonSeparated
    else
      (if (isToday(date)) fmtNoDay else fmt).print(date)

  def isToday(d1: DateTime) = {
     val now = new DateTime()
     d1.getDayOfYear == now.getDayOfYear && d1.getYear == now.getYear
  }

  def toAge(date: DateTime): Long = secondsBetween(date, new DateTime).getSeconds

  def twoDigitNum(num: java.lang.Long) = String.format("%02d", num)
  
}

