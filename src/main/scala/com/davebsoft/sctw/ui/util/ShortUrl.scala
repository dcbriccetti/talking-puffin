package com.davebsoft.sctw.ui.util

import java.awt.{Rectangle, Point}
import java.net.{HttpURLConnection, URL}
import javax.swing.text.JTextComponent
import javax.swing.{SwingWorker, JScrollPane}

/**
 * URL shortening and expanding.
 * 
 * @author Dave Briccetti
 */
object ShortUrl {
  val domains = List("bit.ly", "ff.im", "is.gd", "tinyurl.com", "tr.im")
  val redirectionCodes = List(301, 302)
  type UrlReady = ResourceReady[String,Option[String]]
  val fetcher = new BackgroundResourceFetcher[String, Option[String]](processResult) {
    protected def getResourceFromSource(key: String) = ShortUrl.getNewLocation(key)
  }
  
  private def getNewLocation(url: String): Option[String] = {
    val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("HEAD")
    conn.setInstanceFollowRedirects(false)
    if (redirectionCodes.contains(conn.getResponseCode)) Some(conn.getHeaderField("Location")) else None
  }
  
  def substituteExpandedUrls(text: String, target: JTextComponent) {
    val matcher = LinkExtractor.hyperlinkPattern.matcher(text)
    while (matcher.find) {
      val shortUrl = matcher.group(1)
      if (domains.filter(domain => shortUrl.contains(domain)).length > 0) {
        fetcher.getCachedObject(shortUrl) match {
          case Some(optLongUrl) => optLongUrl match {
            case Some(longUrl) => substituteExpanded(target, shortUrl, longUrl)
            case None => // Cached value is None (short URL couldn’t be expanded)
          }
          case None => fetcher.requestItem(new FetchRequest(shortUrl, target))
        }
      }
    }
  }
  
  private def processResult(urlReady: UrlReady) = urlReady.resource match {
    case Some(location) => substituteExpanded(urlReady.id.asInstanceOf[JTextComponent], urlReady.key, location)
    case None =>
  }
  
  private def substituteExpanded(target: JTextComponent, shortUrl: String, location: String) = {
    val beforeText = target.getText
    val afterText = beforeText.replace(shortUrl, location)
    if (beforeText != afterText) {
      target setText afterText
      target scrollRectToVisible new Rectangle(0,0,1,1) // TODO get this scroll to top working
    }
  }
}