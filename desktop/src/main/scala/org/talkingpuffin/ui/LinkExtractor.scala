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
  val usernameRegex = """@(\w+)"""
  val usernameUrl = "http://twitter.com/$1"
  val hyperlinkPattern = Pattern.compile(hyperlinkRegex)
  val usernamePattern = Pattern.compile(usernameRegex)

  /**
   * Returns a list of tuples of (title, hyperlink) built from:
   * <ol>
   * <li>The in_reply_to_status_id
   * <li>@usernames
   * <li>Hyperlinks
   * </ol> 
   */
  def getLinks(status: TwitterStatus, users: Boolean, pages: Boolean): List[(String,String)] = {
    var urls: List[(String,String)] = List()
    
    if (users) {
      val replyTo = status.inReplyToStatusId
      getReplyToUser(status.text) match {
        case Some(user) => if (replyTo != 0) {
          val url = getStatusUrl(replyTo, user)
          urls = ("Status " + replyTo + " of " + user, url) :: urls
        } 
        case None =>        
      }
  
      val m = usernamePattern.matcher(status.text)
      while (m.find) {
        val userName = m.group(1)
        urls = (userName, "http://twitter.com/" + userName) :: urls
      }
    }
    
    if (pages) {
      val m = hyperlinkPattern.matcher(status.text)
      while (m.find) {
        val url = m.group(1)
        urls = (url, url) :: urls
      }
    }
    
    urls reverse
  }
  
  def getStatusUrl(replyTo: Long, replyToUser: String): String = 
    "http://twitter.com/" + replyToUser + "/statuses/" + replyTo

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