package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import java.text.NumberFormat
import org.talkingpuffin.apix.PartitionedTweets
import twitter4j.User
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.user.UserAnalysis
import org.talkingpuffin.util.WordCounter

object GeneralUserInfo {
  case class ScreenNames(names: List[String])
  case class InfoLine(heading: String, value: Any)

  def create(user: User, screenName: String, pt: PartitionedTweets): List[InfoLine] = {
    val ua = UserAnalysis(pt)
    var msgs = List[InfoLine]()
    val fmt = NumberFormat.getInstance
    fmt.setMaximumFractionDigits(1)
    def disp[T](heading: String, value: T) = msgs = InfoLine(heading, value) :: msgs
    disp("Name", user.getName + " (" + user.getScreenName + ")")
    disp("Location", user.getLocation)
    disp("Description", user.getDescription)
    disp("Followers", fmt.format(user.getFollowersCount))
    disp("Following", fmt.format(user.getFriendsCount))
    disp("Tweets analyzed", ua.numTweets)
    disp("Range", ua.range.getDays + " days")
    disp( "Avg per day", fmt.format(ua.avgTweetsPerDay))
    if (ua.numReplies > 0)
      disp("Avg excluding replies", fmt.format(ua.avgTweetsPerDayExcludingReplies))
    if (ua.numLinks > 0)
      disp("Links in tweets", ua.numLinks + " (" + ua.links.distinct.size + " unique)")
    if (ua.numUsers > 0)
      disp("Users mentioned", ua.numUsers + " (" + ua.users.distinct.size + " unique)")
    disp("Clients", ua.clients.map(_.name).mkString(", "))

    def dispFreq(title: String, bmap: WordCounter.FreqToStringsMap, fn: (List[String]) => Any, minFreq: Int): Unit =
      bmap match {
        case freqs if ! freqs.isEmpty =>
          disp(title, "")
          for (freq <- freqs.keysIterator.filter(_ > minFreq).toList.sorted.reverse)
            disp(freq.toString, fn(freqs.get(freq).get.sorted))
        case _ =>
      }

    dispFreq("Screen name frequencies", ua.screenNamesCounter.frequencies, (l) => ScreenNames(l), 0)
    dispFreq("Word frequencies", ua.tweetsWordCounter.frequencies, (l) => l.mkString(", "), 2)

    msgs.reverse
  }
}
