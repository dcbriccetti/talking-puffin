package com.davebsoft.sctw.ui

import _root_.scala.xml.NodeSeq
import java.util.regex.Pattern

/**
 * Extracts links from Twitter status
 * @author Dave Briccetti
 */

object LinkExtractor {
  
  val hyperlinkRegex = """(https?://[^'"\s]+)"""
  val usernameRegex = """@([^\s:.,]+)"""
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
  def getAllLinks(status: NodeSeq): List[String] = {
    var urls: List[String] = List()
    
    val replyTo = (status \ "in_reply_to_status_id").text
    val replyToUser = getReplyToUser((status \ "text").text)
    if (replyTo.length > 0 && replyToUser.length > 0) {
      urls = getStatusUrl(replyTo, replyToUser) :: urls; 
    }

    var m = usernamePattern.matcher((status \ "text").text)
    while (m.find) {
      urls = "http://twitter.com/" + m.group(1) :: urls
    }
    
    m = hyperlinkPattern.matcher((status \ "text").text)
    while (m.find) {
      urls = m.group(1) :: urls
    }
    
    urls reverse
  }
  
  def getStatusUrl(replyTo: String, replyToUser: String): String = {
    "http://twitter.com/" + replyToUser + "/statuses/" + replyTo
  }

  /**
   * Returns the Twitter handle of the user whose @handle appears at the beginning of 
   * the tweet, or an empty string.
   */
  def getReplyToUser(text: String): String = {
    val m = java.util.regex.Pattern.compile("^@(\\S+)").matcher(text)
    if (m.find) m.group(1) else ""
  }
} 