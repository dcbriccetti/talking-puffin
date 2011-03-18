package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import java.text.NumberFormat
import org.talkingpuffin.apix.PartitionedTweets
import twitter4j.User
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.user.UserAnalysis
import org.talkingpuffin.util.WordCounter.FreqToStringsMap
import org.talkingpuffin.snippet.LineCollector.InfoLine

object GeneralUserInfo {
  case class ScreenNames(names: List[String])
  case class Link(url: String) {
    override def toString = Link.stripFront(url)
  }
  object Link {
    def stripFront(string: String) = string.replaceAll("https?://(www\\.)?", "")
  }

  def create(user: User, screenName: String, pt: PartitionedTweets, ua: UserAnalysis): List[InfoLine] = {
    val lc = new LineCollector

    val fmt = NumberFormat.getInstance
    fmt.setMaximumFractionDigits(2)
    lc.disp("Name", user.getName + " (" + user.getScreenName + ")")
    lc.disp("Location", user.getLocation)
    lc.disp("Description", user.getDescription)
    lc.disp("Followers", fmt.format(user.getFollowersCount))
    lc.disp("Following", fmt.format(user.getFriendsCount))
    lc.disp("Tweets analyzed", fmt.format(ua.numTweets))
    lc.disp("Range", ua.range.getDays + " days")
    lc.disp( "Avg per day", fmt.format(ua.avgTweetsPerDay) + (
      if (ua.numReplies > 0)
        " (" + fmt.format(ua.avgTweetsPerDayExcludingReplies) + " excluding replies)"
      else
        ""
      ))
    if (ua.numLinks > 0)
      lc.disp("Links in tweets", ua.numLinks + " (" + ua.links.distinct.size + " unique)")
    if (ua.numUsers > 0)
      lc.disp("Users mentioned", ua.numUsers + " (" + ua.users.distinct.size + " unique)")
    lc.disp("Clients", ua.clients.map(_.name).mkString(", "))

    lc.msgs.reverse
  }

  def createScreenNameFreq(ua: UserAnalysis) =
    displayFrequencies(ua.screenNamesCounter.frequencies, 0) {
      ScreenNames(_)
    }

  def createWordFreq(ua: UserAnalysis) =
    displayFrequencies(ua.tweetsWordCounter.frequencies, 2) {
      _.mkString(", ")
    }

  def createHashtagFreq(ua: UserAnalysis) =
    displayFrequencies(ua.tweetsHashtagCounter.frequencies, 0) {
      _.mkString(", ")
    }

  private def displayFrequencies(freqToStringsMap: FreqToStringsMap, minFreq: Int)
                      (formatItems: (List[String]) => AnyRef): List[InfoLine] = {
    val lc = new LineCollector
    freqToStringsMap.keysIterator.filter(_ > minFreq).toList.sorted.reverse.foreach(freq =>
      lc.disp(freq.toString, formatItems(freqToStringsMap.get(freq).get.sorted))
    )
    lc.msgs.reverse
  }

  def links(ua: UserAnalysis) = ua.links.map(_.link).distinct.map(link => Link(link)).sortBy(_.toString)

}
