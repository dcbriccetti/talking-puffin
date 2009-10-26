package org.talkingpuffin.ui.util

import scala.util.matching.Regex
import scala.io.Source
import org.talkingpuffin.ui.SwingInvoke
import org.talkingpuffin.util.Loggable

/**
 * Browses a link, also browsing the “target” link for those cases like where a FriendFeed
 * post with a link twitters a ff.im/xx shortened link to FriendFeed. In many cases people 
 * will want to see the ultimate page without stopping at FriendFeed. 
 * 
 * This log excerpt illustrates what happens:
 *
 * <pre> 
 * [Thread-5] DEBUG LinkUnIndirector  - http://ff.im/auf8d starts with ff.im
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
    IndirectedLink("ff.im", "http://friendfeed.com/", """.*<div class="text">.*?<a .*?href="(.*?)".*""".r)
  )

  /**
   * Browse the specified URL, and, if it indirectly contains a “target” URL, browse that URL too.
   */
  def browse(url: String) {
    indirectedLinks find(il => url.contains(il.shortenedUrlPart)) match {
      case Some(il) =>
        debug(url + " starts with " + il.shortenedUrlPart)
        
        new Thread(new Runnable { // Can’t tie up GUI, so new thread here to look for HTTP redirect
          def run = {
            ShortUrl.getExpandedUrls(url, (shortUrl: String, expandedUrl: String) => {
              DesktopUtil.browse(expandedUrl)
              
              if (expandedUrl.startsWith(il.expandedUrlPart)) {
                debug(il.shortenedUrlPart + " expands to " + il.expandedUrlPart)
                SwingInvoke.execSwingWorker ({
                  Source.fromURL(expandedUrl).getLines.map(_.trim).mkString(" ") match {
                    case il.targetLinkRegex(realUrl) => 
                      debug("Target link " + realUrl + " found in " + expandedUrl)
                      Some(realUrl)
                    case _ => None
                  }
                }, {(url: Option[String]) => url match { 
                  case Some(u) => DesktopUtil.browse(u)
                  case None =>
                }})
              }
            })
          }
        }).start
      case None => DesktopUtil.browse(url)
    }
  }
}
