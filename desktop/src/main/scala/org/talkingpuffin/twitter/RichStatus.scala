package org.talkingpuffin.twitter

import org.joda.time.DateTime
import twitter4j.Status

case class RichStatus(status: Status) {
  def inReplyToScreenName: Option[String] = makeOption(status.getInReplyToScreenName)
  def inReplyToStatusId  : Option[Long]   = makeOption(status.getInReplyToStatusId)
  def retweet            : Option[Status] = makeOption(status.getRetweetedStatus)
  def createdAt = new DateTime(status.getCreatedAt)

  private def makeOption[T](value: Any): Option[T] = value match {
    case null => None
    case "" => None
    case -1 => None
    case t: T => Some(t)
  }
}

object RichStatus {
  implicit def asRichStatus(status: Status): RichStatus = RichStatus(status)
}
