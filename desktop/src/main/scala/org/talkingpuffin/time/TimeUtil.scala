package org.talkingpuffin.time

import java.util.Date
import org.joda.time.DateTime
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.ui.HtmlFormatter.htmlAround
import org.talkingpuffin.util.TimeUtil2

object TimeUtil {

  def formatAge(date: Date): String = formatAge(new DateTime(date))

  def formatAge(date: DateTime): String = TimeUtil2.formatAge(date, showAsAge_?)

  def showAsAge_? = GlobalPrefs.isOn(PrefKeys.SHOW_TWEET_DATE_AS_AGE)

  def asHTML(time: Date): String = htmlAround("<font size='-1' face='helvetica'>" +
     formatAge(time) + "</font>")
}
