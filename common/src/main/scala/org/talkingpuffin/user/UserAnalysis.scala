package org.talkingpuffin.user

import scala.collection.JavaConversions._
import org.joda.time.{DateTime, Days}
import org.talkingpuffin.apix.PartitionedTweets
import org.talkingpuffin.util.{WordCounter, LinkExtractor}
import org.talkingpuffin.apix.RichStatus._

case class UserAnalysis(pt: PartitionedTweets) {
  val times = pt.tweets.map(_.getCreatedAt.getTime)
  val oldestTime = new DateTime(times.min)
  val newestTime = new DateTime(times.max)
  val range = Days.daysBetween(oldestTime, newestTime)

  val numTweets = pt.tweets.size
  val numReplies = pt.replies.size
  val numNonReplies = numTweets - numReplies
  val avgTweetsPerDay = numTweets.toDouble / range.getDays
  val avgTweetsPerDayExcludingReplies = numNonReplies.toDouble / range.getDays

  val links = pt.tweets.flatMap(t => LinkExtractor.getLinks(t.getText, None, false, true, false))
  val numLinks = links.size

  val users = pt.tweets.flatMap(t => LinkExtractor.getLinks(t.getText, None, true, false, false))
  val numUsers = users.size

  val allTweetText = pt.tweets.map(_.text).mkString(" ")
  val buckets = WordCounter(allTweetText).frequencies
}
