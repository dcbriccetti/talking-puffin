package com.davebsoft.sctw.ui.util

import java.awt.{Rectangle, Point}
import java.net.{HttpURLConnection, URL}
import javax.swing.text.JTextComponent
import javax.swing.{SwingWorker, JScrollPane}

/**
 * URL shortening and expanding.
 * @author Dave Briccetti
 */
object ShortUrl {
  val domains = List("bit.ly", "ff.im", "is.gd", "tinyurl.com", "tr.im")
  val redirectionCodes = List(301, 302)
  type UrlReady = ResourceReady[String,Option[String]]
  val fetcher = new BackgroundResourceFetcher[String, Option[String]](processResult) {
    protected def getResourceFromSource(key: String) = ShortUrl.expand(key)
  }
  
  def expand(url: String): Option[String] = {
    val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("HEAD")
    conn.setInstanceFollowRedirects(false)
    if (redirectionCodes.contains(conn.getResponseCode)) Some(conn.getHeaderField("Location")) else None
  }
  
  def substituteExpandedUrls(text: String, target: JTextComponent) {
    val m = LinkExtractor.hyperlinkPattern.matcher(text)
    while (m.find) {
      val url = m.group(1)
      if (ShortUrl.domains.filter(s => url.contains(s)).length > 0) {
        fetcher.getCachedObject(url) match {
          case Some(optLocation) => optLocation match {
            case Some(loc) => sub(target, url, loc)
            case None => // Cached value is None (short URL couldn’t be expanded)
          }
          case None => fetcher.requestItem(new FetchRequest(url, target))
        }
      }
    }
  }

  def processResult(urlReady: UrlReady) = {
    urlReady.resource match {
      case Some(location) =>
        val target = urlReady.id.asInstanceOf[JTextComponent]
        sub(target, urlReady.key, location)
      case None =>
    }
  }
  
  def sub(target: JTextComponent, shortUrl: String, location: String) = {
    val beforeText = target.getText
    val afterText = beforeText.replace(shortUrl, location)
    if (beforeText != afterText) {
      target setText afterText
      target scrollRectToVisible new Rectangle(0,0,1,1) // TODO get this scroll to top working
    }
    
  }
}