package org.talkingpuffin.apix

import org.joda.time.DateTime
import twitter4j.Status

/**
 * The twitter4j.Status class, augmented with additional features for application
 * and Scala suitability.
 */
case class RichStatus(status: Status) extends OptionMaker {
  def inReplyToScreenName: Option[String] = makeOption(status.getInReplyToScreenName)
  def inReplyToStatusId  : Option[Long]   = makeOption(status.getInReplyToStatusId)
  def inReplyToUserId    : Option[Long]   = makeOption(status.getInReplyToUserId)
  def retweet            : Option[Status] = makeOption(status.getRetweetedStatus)

  def retweetOrTweet = retweet.getOrElse(status)
  def createdAt = new DateTime(status.getCreatedAt)

  def text = retweetOrTweet.getText
  
  def source = status.getSource
  def sourceName = SourceDetails(source).name
  def sourceUrl = SourceDetails(source).url
}

object RichStatus {
  implicit def asRichStatus(status: Status): RichStatus = RichStatus(status)
}
