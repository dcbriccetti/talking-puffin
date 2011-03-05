package org.talkingpuffin.snippet

import scala.collection.JavaConversions._
import org.joda.time.{DateTime, Days}
import java.text.NumberFormat
import org.talkingpuffin.apix.PartitionedTweets
import twitter4j.User
import org.talkingpuffin.util.{WordCounter, LinkExtractor}
import org.talkingpuffin.apix.RichStatus._

object GeneralUserInfo {
  def create(uinfo: User, screenName: String, pt: PartitionedTweets): List[String] = {
    val fmt = NumberFormat.getInstance
    var msgs = List[String]()
    def disp(msg: String) = msgs = msg :: msgs
    disp(uinfo.getName + " (" + uinfo.getScreenName + ")")
    disp(uinfo.getLocation)
    disp(uinfo.getDescription)
    disp("Followers: " + fmt.format(uinfo.getFollowersCount) +
      ", following: " + fmt.format(uinfo.getFriendsCount))
    val times = pt.tweets.map(_.getCreatedAt.getTime)
    val oldestTime = new DateTime(times.min)
    val newestTime = new DateTime(times.max)
    val range = Days.daysBetween(oldestTime, newestTime)
    val numTweets = pt.tweets.size
    val numReplies = pt.replies.size
    disp("The last " + numTweets + " tweets span " + range.getDays + " days, for an average of " +
      fmt.format(numTweets.toDouble / range.getDays) + " tweets/day" +
      (if (numReplies > 0) ", or " +
        fmt.format((numTweets.toDouble - numReplies) / range.getDays) + " tweets/day if you don’t count the " +
        fmt.format(numReplies) + " replies"
      else "")
    )
    val links = pt.tweets.flatMap(t => LinkExtractor.getLinks(t.getText, None, false, true, false))
    val numLinks = links.size
    if (numLinks > 0)
      disp("Links in tweets: " + numLinks + " (" + links.distinct.size + " unique)")
    val users = pt.tweets.flatMap(t => LinkExtractor.getLinks(t.getText, None, true, false, false))
    val numUsers = users.size
    if (numUsers > 0)
      disp("Users mentioned: " + numUsers + " (" + users.distinct.size + " unique)")
    val buckets = WordCounter.count(pt.tweets.map(_.text).mkString(" "))
    disp("Word frequencies:")
    for (freq <- buckets.keysIterator.filter(_ > 2).toList.sorted.reverse)
      disp(freq.toString + ": " + buckets.get(freq).get.sorted.mkString(", "))
    msgs.reverse
  }
}