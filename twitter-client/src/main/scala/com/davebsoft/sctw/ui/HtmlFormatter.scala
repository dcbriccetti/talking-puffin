package com.davebsoft.sctw.ui
import com.davebsoft.sctw.twitter.Utils

/**
 * Helps with creating HTML for display in the UI
 * @author Dave Briccetti
 */

object HtmlFormatter {

  def createTweetHtml(text: String, replyTo: String): String = {
    val replyToUser = Utils.getReplyToUser(text)   
    val parent = if (replyTo.length > 0 && replyToUser.length > 0) 
      "<a href='http://twitter.com/" + replyToUser + 
      "/statuses/" + replyTo + "'>↑</a> " else "" 
    var r = text.replaceAll("(https?\\://[^'\"\\s]+)", "<a href='$1'>$1</a>")
    r = r.replaceAll("@(\\S+)", "<a href='http://twitter.com/$1'>@$1</a>")
    "<html>" + parent + "<font face='Georgia' size='+2'>" + r + "</font></html>"    
  }

}