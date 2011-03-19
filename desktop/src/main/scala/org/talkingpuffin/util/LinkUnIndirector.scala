package org.talkingpuffin.util

import scala.util.matching.Regex
import scala.io.Source
import org.talkingpuffin.ui.SwingInvoke
import java.net.{HttpURLConnection, URL}

/**
 * Browses the end link in what may be a chain of indirection from the likes of FriendFeed, Digg, and
 * StumbleUpon. In many cases people will want to see the ultimate page without stopping at intermediate
 * points, or having their desired page framed by some service. 
 */ 
object LinkUnIndirector extends Loggable {
  
  /**
   * Does shortenedUrlPart redirect to expandedUrlPart, and when that is fetched, does its contents
   * hold a target link identified by targetLinkRegex?  
   */
  case class IndirectedLink(shortenedUrlPart: String, expandedUrlPart: String, targetLinkRegex: Regex)
  
  /** A list of all known IndirectedLinks */
  val indirectedLinks = {
    val dzoneTarget = """.*<div class="ldTitle">.*?<a .*?href="(.*?)".*""".r
    List(
      IndirectedLink("ff.im", "http://friendfeed.com/", """.*<div class="text">.*?<a .*?href="(.*?)".*""".r),
      IndirectedLink("digg.com", "http://digg.com/", """.*<h1 id="title">.*?<a .*?href="(.*?)".*""".r),
      IndirectedLink("bit.ly", "http://www.dzone.com/", dzoneTarget),
      IndirectedLink("dzone.com", "http://www.dzone.com/", dzoneTarget)
    )
  }
  
  /**
   * Finds the target(s) of the specified URL, bypassing any “wrappers” 
   * like FriendFeed, Digg, and StumbleUpon. In the case of the 
   * <code>IndirectedLink</code>s (such as FriendFeed), the found
   * callback will be called twice: once for the intermediate page, and once 
   * for the target page.
   */
  def findLinks(foundCallback: (String) => Unit, notFoundCallback: (String) => Unit)(url: String) {
    if (ShortUrl.wrapperBypassableWithSimpleRedirection(new URL(url).getHost)) {
      ShortUrl.getExpandedUrl(url, foundCallback)
    } else {
      indirectedLinks.find(link => url.contains(link.shortenedUrlPart)) match {
        case Some(il) =>
          debug(url + " contains " + il.shortenedUrlPart)

          new Thread(new Runnable { // Can’t tie up GUI, so new thread here
            def run = {
              ShortUrl.getExpandedUrl(url, (expandedUrl: String) => {
                foundCallback(expandedUrl)
                if (expandedUrl.startsWith(il.expandedUrlPart)) {
                  readIntermediatePageAndFindTargetUrl(expandedUrl, il.targetLinkRegex, foundCallback)
                }
              })
            }
          }).start
        case None => notFoundCallback(url)
      }
    }
  }
  
  private def readIntermediatePageAndFindTargetUrl(expandedUrl: String,
      regex: Regex, expandedFound: (String) => Unit): Unit = {
    // ShortUrl.getExpandedUrls has called us in the GUI event thread, so we need
    // another thread here to fetch the HTML page.
    SwingInvoke.execSwingWorker ({
      val conn = new URL(expandedUrl).openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestProperty("User-agent", "TalkingPuffin")
      Source.fromInputStream(conn.getInputStream).getLines.map(_.trim).mkString(" ") match {
        case regex(realUrl) => 
          debug("Target link " + realUrl + " found in " + expandedUrl)
          Some(realUrl)
        case _ => 
          debug("Target link not found in " + expandedUrl)
          None
      }
    }, {(url: Option[String]) => url.foreach(u =>
      expandedFound(u)
    )})
  }
}
  