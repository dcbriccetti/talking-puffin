package com.davebsoft.sctw.ui
import _root_.scala.xml.NodeSeq
import java.util.regex.Pattern

/**
 * Helps with creating HTML for display in the UI
 * @author Dave Briccetti
 */

object HtmlFormatter {

  def createTweetHtml(text: String, replyTo: String): String = {
    val arrowLinkToParent = LinkExtractor.getReplyToUser(text) match {
      case Some(user) => {
        if (replyTo.length > 0) {
          "<a href='" + LinkExtractor.getStatusUrl(replyTo, user) + "'>↑</a> " 
        } else ""
      }
      case None => ""
    }
              
    var r = text.replaceAll(LinkExtractor.hyperlinkRegex, "<a href='$1'>$1</a>")

    r = r.replaceAll(LinkExtractor.usernameRegex, "<a href='" + LinkExtractor.usernameUrl + "'>@$1</a>")

    "<html>" + arrowLinkToParent + "<font face='Georgia' size='+2'>" + r + "</font></html>"
  }

}

