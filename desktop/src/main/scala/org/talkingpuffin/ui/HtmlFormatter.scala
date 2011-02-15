package org.talkingpuffin.ui

import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Constants.RetweetSymbol
import twitter4j.User

/**
 * Helps with creating HTML for display in the UI
 */
object HtmlFormatter extends Loggable {

  def createTweetHtml(text: String, replyTo: Option[Long], source: String, retweetingUser: Option[User]): String = {
    val arrowLinkToParent = LinkExtractor.getReplyToInfo(replyTo, text) match {
      case Some((user, id)) => "<a href='" + LinkExtractor.getStatusUrl(id, user) + "'>↑</a> " 
      case None => ""
    }
              
    val r = LinkExtractor.createLinks(text).replaceAll("\n", "<br/>")

    htmlAround(arrowLinkToParent + fontAround(r, "190%") + fontAround(
        (if (retweetingUser.isDefined) " " + RetweetSymbol + " " + retweetingUser.get.getScreenName else "") +
          " " + source, "80%"))
  }
  
  def fontAround(s: String, size: String) = 
    "<font style='font-family: Georgia; font-size: " + size + "'>" + s + "</font>"

  def htmlAround(s: String) = "<html>" + s + "</html>"

}

