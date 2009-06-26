package org.talkingpuffin.ui

import _root_.scala.xml.NodeSeq
import java.util.regex.Pattern
import org.talkingpuffin.twitter.{TwitterStatus}

/**
 * Extracts links from Twitter status
 */

object LinkExtractor {
  
  val urlCharClass = """[^'"()\[\]\s]"""
  val hyperlinkRegex = "(https?://" + urlCharClass + "+)"
  val usernameRegex = """@([^\s:.,()]+)"""
  val usernameUrl = "http://twitter.com/$1"
  val hyperlinkPattern = Pattern.compile(hyperlinkRegex)
  val usernamePattern = Pattern.compile(usernameRegex)

  /**
   * Returns a list of hyperlink strings built from:
   * <ol>
   * <li>The in_reply_to_status_id
   * <li>@usernames
   * <li>Hyperlinks
   * </ol> 
   */
  def getLinks(status: TwitterStatus, users: Boolean, pages: Boolean): List[String] = {
    var urls: List[String] = List()
    
    if (users) {
      val replyTo = status.inReplyToStatusId.toString
      getReplyToUser(status.text) match {
        case Some(user) => if (replyTo.length > 0) urls = getStatusUrl(replyTo, user) :: urls; 
        case None =>        
      }
  
      val m = usernamePattern.matcher(status.text)
      while (m.find) {
        urls = "http://twitter.com/" + m.group(1) :: urls
      }
    }
    
    if (pages) {
      val m = hyperlinkPattern.matcher(status.text)
      while (m.find) {
        urls = m.group(1) :: urls
      }
    }
    
    urls reverse
  }
  
  def getStatusUrl(replyTo: String, replyToUser: String): String = {
    "http://twitter.com/" + replyToUser + "/statuses/" + replyTo
  }

  val replyToUserPattern = Pattern.compile("^@(\\S+)")
  
  /**
   * Returns the Twitter handle of the user whose @handle appears at the beginning of 
   * the tweet, or an empty string.
   */
  def getReplyToUser(text: String): Option[String] = {
    val m = replyToUserPattern.matcher(text)
    if (m.find) Some(m.group(1)) else None
  }

  val withoutUserPattern = Pattern.compile("""^@\S+ (.*)""")
  
  /**
   * Returns a string with any @user at the beginning removed.
   */
  def getWithoutUser(text: String): String = {
    val m = withoutUserPattern.matcher(text)
    if (m.find) m.group(1) else text
  }
} 