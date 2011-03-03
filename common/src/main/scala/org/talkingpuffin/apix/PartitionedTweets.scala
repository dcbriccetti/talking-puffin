package org.talkingpuffin.apix

import scala.collection.JavaConversions._
import twitter4j.{Paging, Twitter}
import org.talkingpuffin.filter.RetweetDetector
import org.talkingpuffin.apix.PageHandler._
import org.talkingpuffin.apix.RichStatus._

/**
 * Partition tweets into several distinct categories.
 */
case class PartitionedTweets(tw: Twitter, screenName: String) {
  private val paging = new Paging
  paging.setCount(Constants.MaxItemsPerRequest)
  val tweets = userTimeline(tw, screenName)(paging) // Get one page for now

  val (newStyleRts, notNewStyleRts) = tweets.partition(_.isRetweet)
  val (oldStyleRts, notOldStyleRts) =
    notNewStyleRts.partition(st => RetweetDetector(st.text).isRetweet)
  val (replies, plainTweets) = notOldStyleRts.partition(_.inReplyToUserId.isDefined)
}
