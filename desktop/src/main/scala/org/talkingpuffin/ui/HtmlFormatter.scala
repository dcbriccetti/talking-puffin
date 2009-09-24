package org.talkingpuffin.ui
import _root_.scala.xml.NodeSeq
import java.util.regex.Pattern

/**
 * Helps with creating HTML for display in the UI
 */

object HtmlFormatter {

  def createTweetHtml(text: String, replyTo: Option[Long], source: String): String = {
    val arrowLinkToParent = LinkExtractor.getReplyToInfo(replyTo, text) match {
      case Some((user, id)) => "<a href='" + LinkExtractor.getStatusUrl(id, user) + "'>↑</a> " 
      case None => ""
    }
              
    var r = text.replaceAll(LinkExtractor.hyperlinkRegex, "<a href='$1'>$1</a>")

    r = r.replaceAll(LinkExtractor.usernameRegex, "<a href='" + LinkExtractor.usernameUrl + "'>@$1</a>")

    htmlAround(arrowLinkToParent + fontAround(r, "+2") + fontAround(" from " + source, "-1"))
  }
  
  def fontAround(s: String, size: String): String = {
    "<font face='Georgia' size='" + size + "'>" + s + "</font>"
  }

  def htmlAround(s: String): String = {
    "<html>" + s + "</html>"
  }

}

