package org.talkingpuffin.user

import scala.collection.JavaConversions._
import org.joda.time.{DateTime, Days}
import org.talkingpuffin.apix.PartitionedTweets
import org.talkingpuffin.util.{WordCounter, LinkExtractor}
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.util.LinkExtractor.Link

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

  val clients = pt.tweets.map(_.sourceDetails).distinct.sortBy(_.name.toLowerCase)

  val links: Seq[Link] = pt.tweets.flatMap(tweet => LinkExtractor.getLinks(tweet.getText, None, links = true))
  val numLinks = links.size

  val users: Seq[Link] = pt.tweets.flatMap(tweet => LinkExtractor.getLinks(tweet.getText, None, users = true))
  val numUsers = users.size

  val allTweetText = pt.tweets.map(_.text).mkString(" ")
  val tweetsWordCounter = WordCounter(allTweetText)
  val tweetsHashtagCounter = WordCounter(allTweetText, wordFilter = {(word: String) => word(0) == '#'},
    wordProcessor = {(word: String) => word.takeRight(word.length - 1)})

  val screenNamesCounter = WordCounter(users.map(_.title).mkString(" "))
}
