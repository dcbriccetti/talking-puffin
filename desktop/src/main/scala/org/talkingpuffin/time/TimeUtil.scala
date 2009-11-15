package org.talkingpuffin.time


import java.util.Date
import javax.swing.border.EmptyBorder
import org.joda.time.{DateTime, Duration, DateMidnight, Seconds}
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.joda.time.format.DateTimeFormat
import org.talkingpuffin.ui.HtmlFormatter

object TimeUtil{
   val fmt = DateTimeFormat.forPattern("MM/dd HH:mm:ss")
  val fmtNoDay = DateTimeFormat.forPattern("HH:mm:ss")
  def formatAge(date: Date) : String  = formatAge(new DateTime(date))
  def formatAge(date: DateTime): String = {
    if (showAsAge_?)
      new TimeFormatter(toAge(date)).colonSeparated
    else
      if (isToday(date)) fmtNoDay.print(date) else fmt.print(date)
  }
  def showAsAge_? = GlobalPrefs.isOn(PrefKeys.SHOW_TWEET_DATE_AS_AGE)
  def isToday(d1: DateTime):Boolean = d1.isAfter(new DateMidnight)
  def toAge(date: DateTime) : Long=  Seconds.secondsBetween(date,now).getSeconds
  def now = new DateTime

  def asHTML(value : Any) : String = asHTML(value.asInstanceOf[Date])
  def asHTML(time :  Date) :String = {
   HtmlFormatter.htmlAround("<font size='-1' face='helvetica'>" +
       formatAge(time) + "</font>")
   }


}