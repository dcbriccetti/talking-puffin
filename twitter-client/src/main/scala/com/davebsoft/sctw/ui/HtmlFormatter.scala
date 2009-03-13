package com.davebsoft.sctw.ui
import com.davebsoft.sctw.twitter.Utils

/**
 * Helps with creating HTML for display in the UI
 * @author Dave Briccetti
 */

object HtmlFormatter {

  def createTweetHtml(text: String, replyTo: String): String = {
    val replyToUser = Utils.getReplyToUser(text)
    
    val arrowLinkToParent = if (replyTo.length > 0 && replyToUser.length > 0) 
      "<a href='http://twitter.com/" + replyToUser + 
      "/statuses/" + replyTo + "'>↑</a> " else "" 
    
    var r = text.replaceAll("""(https?://[^'"\s]+)""", "<a href='$1'>$1</a>")
    
    r = r.replaceAll("""@([^\s:.,]+)""", "<a href='http://twitter.com/$1'>@$1</a>")
    
    "<html>" + arrowLinkToParent + "<font face='Georgia' size='+2'>" + r + "</font></html>"    
  }

}