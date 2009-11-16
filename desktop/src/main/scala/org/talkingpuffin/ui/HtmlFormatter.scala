package org.talkingpuffin.ui

import org.talkingpuffin.util.Loggable

/**
 * Helps with creating HTML for display in the UI
 */
object HtmlFormatter extends Loggable {

  def createTweetHtml(text: String, replyTo: Option[Long], source: String): String = {
    val arrowLinkToParent = LinkExtractor.getReplyToInfo(replyTo, text) match {
      case Some((user, id)) => "<a href='" + LinkExtractor.getStatusUrl(id, user) + "'>↑</a> " 
      case None => ""
    }
              
    val r = LinkExtractor.createLinks(text)

    htmlAround(arrowLinkToParent + fontAround(r, "190%") + fontAround(" from " + source, "80%"))
  }
  
  def fontAround(s: String, size: String) = 
    "<font style='font-family: Georgia; font-size: " + size + "'>" + s + "</font>"

  def htmlAround(s: String) = "<html>" + s + "</html>"

}

