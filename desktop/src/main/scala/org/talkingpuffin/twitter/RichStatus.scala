package org.talkingpuffin.twitter

import org.joda.time.DateTime
import twitter4j.Status

case class RichStatus(status: Status) {
  def inReplyToScreenName: Option[String] = makeOption(status.getInReplyToScreenName)
  def inReplyToStatusId  : Option[Long]   = makeOption(status.getInReplyToStatusId)
  def inReplyToUserId    : Option[Long]   = makeOption(status.getInReplyToUserId)
  def retweet            : Option[Status] = makeOption(status.getRetweetedStatus)
  def retweetOrTweet     : Status = retweet.getOrElse(status)
  def createdAt = new DateTime(status.getCreatedAt)

  def source = status.getSource
  def sourceName = extractSource(source).name
  def sourceUrl = extractSource(source).url

  private def makeOption[T](value: Any): Option[T] = value match {
    case null => None
    case "" => None
    case -1 => None
    case t: T => Some(t)
  }

  private case class SourceDetails(raw: String, url: Option[String], name: String)

  /**
   * From the “source” string, which oddly may contain either a simple string, such as “web,”
   * or an anchor tag with an href and a source name, extract:
   * <ol>
   * <li>the entire contents into {@link #source}, for backward compatibility
   * <li>a URL, if found, into {@link #sourceUrl}
   * <li>the source name into {@link #sourceName}
   * </ol>
   *
   */
  private def extractSource(text: String): SourceDetails = {
    // XML.loadString might have been used instead of this regex, but it throws exceptions because of the contents
    val anchorRegex = """<a.*href=["'](.*?)["'].*?>(.*?)</a>""".r
    val (url, name) = text match {
      case anchorRegex(u,s) => (Some(u), s)
      case _ => (None, text)
    }
    SourceDetails(text, url, name)
  }
}

object RichStatus {
  implicit def asRichStatus(status: Status): RichStatus = RichStatus(status)
}
