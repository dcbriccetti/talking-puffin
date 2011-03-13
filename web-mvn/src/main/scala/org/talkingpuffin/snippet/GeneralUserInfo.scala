package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import java.text.NumberFormat
import org.talkingpuffin.apix.PartitionedTweets
import twitter4j.User
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.user.UserAnalysis
import org.talkingpuffin.util.WordCounter.FreqToStringsMap
import org.talkingpuffin.snippet.LineCollector.InfoLine
import java.net.URL

object GeneralUserInfo {
  case class ScreenNames(names: List[String])
  case class Link(url: URL) {
    override def toString = url.toString.replaceAll("https?://", "")
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

  def createScreenNameFreq(ua: UserAnalysis) = {
    val lc = new LineCollector
    dispFreq(lc, "Screen name frequencies", ua.screenNamesCounter.frequencies, (l) => ScreenNames(l), 0)
    lc.msgs.reverse
  }

  def createWordFreq(ua: UserAnalysis) = {
    val lc = new LineCollector
    dispFreq(lc, "Word frequencies", ua.tweetsWordCounter.frequencies, (l) => l.mkString(", "), 2)
    lc.msgs.reverse
  }

  def links(ua: UserAnalysis) = ua.links.map(_.link).distinct.map(link => Link(new URL(link))).sortBy(_.toString)

  private def dispFreq(lc: LineCollector, title: String, bmap: FreqToStringsMap,
  fn: (List[String]) => AnyRef, minFreq: Int): Unit =
    bmap match {
      case freqs if ! freqs.isEmpty =>
        lc.disp(title, "")
        for (freq <- freqs.keysIterator.filter(_ > minFreq).toList.sorted.reverse)
          lc.disp(freq.toString, fn(freqs.get(freq).get.sorted))
      case _ =>
    }

}
