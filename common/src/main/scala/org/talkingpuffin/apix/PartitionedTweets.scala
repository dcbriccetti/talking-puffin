package org.talkingpuffin.apix

import scala.collection.JavaConversions._
import twitter4j.{Paging, Twitter}
import org.talkingpuffin.filter.RetweetDetector
import org.talkingpuffin.apix.RichStatus._

case class PartitionedTweets(tw: Twitter, screenName: String) {
  private val paging = new Paging
  paging.setCount(Constants.MaxItemsPerRequest)
  val tweets = PageHandler.userTimeline(tw, screenName)(paging)

  val (newStyleRetweets, notNewStyleRetweets) = tweets.partition(st => st.isRetweet)
  val (oldStyleRetweets, notOldStyleRetweets) =
    notNewStyleRetweets.partition(st => RetweetDetector(st.text).isRetweet)
  val (replies, notReplies) = notOldStyleRetweets.partition(_.inReplyToUserId.isDefined)
}
