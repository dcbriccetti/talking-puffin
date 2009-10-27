package org.talkingpuffin.util

import scala.util.matching.Regex
import scala.io.Source
import org.talkingpuffin.ui.SwingInvoke
import java.net.{HttpURLConnection, URL}

/**
 * Browses a link, also browsing the “target” link for those cases like where a FriendFeed
 * post with a link twitters a ff.im/xx shortened link to FriendFeed. In many cases people 
 * will want to see the ultimate page without stopping at FriendFeed. 
 * 
 * This log excerpt illustrates what happens:
 *
 * <pre> 
 * [AWT-EventQueue-0] DEBUG LinkUnIndirector  - http://ff.im/auf8d starts with ff.im
 * [AWT-EventQueue-0] DEBUG LinkUnIndirector  - ff.im expands to http://friendfeed.com/
 * [SwingWorker-pool-1-thread-9] DEBUG LinkUnIndirector  - 
 *   Target link http://fupeg.blogspot.com/2009/10/social-technology-fail.html found in 
 *   http://friendfeed.com/michaelg/09a8d52e/social-technology-fail
 * </pre>
 */
object LinkUnIndirector extends Loggable {
  
  /**
   * Does shortenedUrlPart redirect to expandedUrlPart, and when that is fetched, does its contents
   * hold a target link identified by targetLinkRegex?  
   */
  case class IndirectedLink(val shortenedUrlPart: String, val expandedUrlPart: String, targetLinkRegex: Regex)
  
  /** A list of all known IndirectedLinks */
  val indirectedLinks = List(
    IndirectedLink("ff.im", "http://friendfeed.com/", """.*<div class="text">.*?<a .*?href="(.*?)".*""".r),
    IndirectedLink("digg.com", "http://digg.com/", """.*<h1 id="title">.*?<a .*?href="(.*?)".*""".r)
  )
  
  /**
   * Browse the specified URL, bypassing any “wrappers” like FriendFeed, Digg, and StumbleUpon.
   */
  def findLinks(expandedFound: (String) => Unit, expandedNotFound: (String) => Unit)(urlString: String) {
    val url = new URL(urlString)
    
    if (ShortUrl.redirectionBypassesWrapper(url.getHost)) {
      ShortUrl.getExpandedUrls(urlString, (_: String, expandedUrl: String) => expandedFound(expandedUrl))
    } else {
      indirectedLinks find(il => urlString.contains(il.shortenedUrlPart)) match {
        case Some(il) =>
          debug(urlString + " contains " + il.shortenedUrlPart)

          new Thread(new Runnable { // Can’t tie up GUI, so new thread here to look for HTTP redirect
            def run = {
              ShortUrl.getExpandedUrls(urlString, (shortUrl: String, expandedUrl: String) => {
                expandedFound(expandedUrl)

                if (expandedUrl.startsWith(il.expandedUrlPart)) {
                  findTarget(expandedUrl, il.targetLinkRegex, expandedFound)
                }
              })
            }
          }).start
        case None => expandedNotFound(urlString)
      }
    }
  }
  
  private def findTarget(expandedUrl: String, regex: Regex, expandedFound: (String) => Unit): Unit = {
    // ShortUrl.getExpandedUrls has called us in the GUI event thread, so we need
    // another thread here to fetch the HTML page.
    SwingInvoke.execSwingWorker ({
      val conn = new URL(expandedUrl).openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestProperty("User-agent", "TalkingPuffin")
      Source.fromInputStream(conn.getInputStream).getLines.map(_.trim).mkString(" ") match {
        case regex(realUrl) => 
          debug("Target link " + realUrl + " found in " + expandedUrl)
          Some(realUrl)
        case _ => None
      }
    }, {(url: Option[String]) => url match { 
      case Some(u) => expandedFound(u)
      case None =>
    }})
    
  }
}
