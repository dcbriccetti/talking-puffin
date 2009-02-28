package com.davebsoft.sctw.ui

/**
 * Helps with creating HTML for display in the UI
 * @author Dave Briccetti
 */

object HtmlFormatter {

  def createTweetHtml(text: String, replyTo: String): String = {
    val replyToUser = getReplyToUser(text)   
    val parent = if (replyTo.length > 0 && replyToUser.length > 0) 
      "<a href='http://twitter.com/" + replyToUser + 
      "/statuses/" + replyTo + "'>↑</a> " else "" 
    var r = text.replaceAll("(https?\\://[^'\"\\s]+)", "<a href='$1'>$1</a>")
    r = r.replaceAll("@(\\S+)", "<a href='http://twitter.com/$1'>@$1</a>")
    "<html><font size='+2'>" + parent + r + "</font></html>"    
  }

  /**
   * Returns the Twitter handle of the user whose @handle appears at the beginning of 
   * the tweet, or an empty string.
   */
  private def getReplyToUser(text: String): String = {
    val m = java.util.regex.Pattern.compile("^@(\\S+)").matcher(text)
    if (m.find) m.group(1) else ""
  }
}