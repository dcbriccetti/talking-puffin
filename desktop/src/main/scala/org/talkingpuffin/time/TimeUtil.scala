package org.talkingpuffin.time

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.Seconds.secondsBetween
import org.joda.time.format.DateTimeFormat
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.ui.HtmlFormatter.htmlAround

object TimeUtil {
  private val fmt = DateTimeFormat.forPattern("MM/dd HH:mm:ss")
  private val fmtNoDay = DateTimeFormat.forPattern("HH:mm:ss")

  def formatAge(date: Date): String = formatAge(new DateTime(date))

  def formatAge(date: DateTime): String =
    if (showAsAge_?)
      new TimeFormatter(toAge(date)).colonSeparated
    else
      (if (isToday(date)) fmtNoDay else fmt).print(date)

  def showAsAge_? = GlobalPrefs.isOn(PrefKeys.SHOW_TWEET_DATE_AS_AGE)

  def isToday(d1: DateTime) = {
     val now = new DateTime()
     d1.getDayOfYear == now.getDayOfYear && d1.getYear == now.getYear
  }

  def toAge(date: DateTime): Long = secondsBetween(date, new DateTime).getSeconds

  def asHTML(time: Date): String = htmlAround("<font size='-1' face='helvetica'>" +
     formatAge(time) + "</font>")
}
