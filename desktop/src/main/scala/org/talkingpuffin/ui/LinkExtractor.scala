package org.talkingpuffin.ui

import java.util.regex.Pattern
import org.talkingpuffin.twitter.{TwitterStatus}

/**
 * Extracts links from Twitter status
 */
object LinkExtractor {
  
  val urlCharClass = """[^'"()\[\]\s]"""
  val hyperlinkRegex = "(https?://" + urlCharClass + "+)"
  val userListRegex = """@((\w+)/""" + urlCharClass + "+)"
  val usernameRegex = """@(\w+)"""
  val usernameUrl = "http://twitter.com/$1"
  val userLinkUrl = "http://twitter.com/$1"
  val hyperlinkPattern = Pattern.compile(hyperlinkRegex)
  val userListPattern = Pattern.compile(userListRegex)
  val usernamePattern = Pattern.compile(usernameRegex)

  /**
   * Returns a list of tuples of (title, hyperlink) built from:
   * <ol>
   * <li>The in_reply_to_status_id
   * <li>@usernames
   * <li>Hyperlinks
   * </ol> 
   */
  def getLinks(status: TwitterStatus, users: Boolean, pages: Boolean, lists: Boolean): List[(String,String)] = {
    var urls: List[(String,String)] = List()
    
    if (users) {
      getReplyToInfo(status.inReplyToStatusId, status.text) match {
        case Some((user, replyTo)) => urls ::= ("Status " + replyTo + " of " + user, getStatusUrl(replyTo, user))
        case _ =>
      }
  
      val m = usernamePattern.matcher(status.text)
      while (m.find) {
        val userName = m.group(1)
        val newItem = (userName, "http://twitter.com/" + userName)
        if (! urls.contains(newItem))
          urls ::= newItem
      }
    }
    
    if (pages) {
      val m = hyperlinkPattern.matcher(status.text)
      while (m.find) {
        val url = m.group(1)
        val newItem = (url, url)
        if (! urls.contains(newItem))
          urls ::= newItem
      }
    }
    
    if (lists) {
      val m = userListPattern.matcher(status.text)
      while (m.find) {
        val list = m.group(1)
        val newItem = (list, "http://twitter.com/" + list)
        if (! urls.contains(newItem))
          urls ::= newItem
      }
    }
    
    urls reverse 
  }
  
  def getStatusUrl(replyTo: Long, replyToUser: String) = 
    "http://twitter.com/" + replyToUser + "/statuses/" + replyTo

  private val replyToUserRegex = (".*?" + usernameRegex + ".*").r
  
  /**
   * Returns the Twitter handle and status ID of the user whose @handle appears at the beginning of 
   * the tweet, None.
   */
  def getReplyToInfo(inReplyToStatusId: Option[Long], text: String): Option[(String,Long)] = 
    inReplyToStatusId match {
      case Some(id) => text match {
        case replyToUserRegex(username) => Some((username, id))
        case _ => None
      }
      case _ => None
    }

  val withoutUserPattern = Pattern.compile("""^@\S+ (.*)""")
  
  /**
   * Returns a string with any @user at the beginning removed.
   */
  def getWithoutUser(text: String): String = {
    val m = withoutUserPattern.matcher(text)
    if (m.find) m.group(1) else text
  }

  private val atCode = "##xX##"
  
  private def wrapUserLinks(text: String) = text.replaceAll(LinkExtractor.usernameRegex, 
      "<a href='" + LinkExtractor.usernameUrl + "'>" + atCode + "$1</a>")

  private def wrapUserLists(text: String) = text.replaceAll(LinkExtractor.userListRegex,
      "<a href='" + LinkExtractor.userLinkUrl + "'>" + atCode + "$1</a>")
  
  private def replaceAtCode(text: String) = text.replaceAll(atCode, "@")

  def createLinks(text: String) = LinkExtractor.replaceAtCode(
      LinkExtractor.wrapUserLinks(LinkExtractor.wrapUserLists(text))) 
} 