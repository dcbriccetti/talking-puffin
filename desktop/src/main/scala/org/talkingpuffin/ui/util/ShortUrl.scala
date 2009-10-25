package org.talkingpuffin.ui.util

import java.net.{HttpURLConnection, URL}
import org.talkingpuffin.ui.LinkExtractor

/**
 * URL shortening and expanding.
 */
object ShortUrl {
  val shortenerDomains = List("bit.ly", "ff.im", "is.gd", "ping.fm", "short.ie", "su.pr", "tinyurl.com", "tr.im")
  val regex = "http://(" + shortenerDomains.map(_.replace(".","""\.""")).mkString("|") + ")/" + 
      LinkExtractor.urlCharClass + "*"
  private val redirectionCodes = List(301, 302)
  private type LongUrlReady = ResourceReady[String,String]
  
  private val fetcher = new BackgroundResourceFetcher[String, String] {
    override def getResourceFromSource(url: String): String = {
      val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestMethod("HEAD")
      conn.setInstanceFollowRedirects(false)
      if (redirectionCodes.contains(conn.getResponseCode)) conn.getHeaderField("Location") else 
        throw new NoSuchResource(url)
    }
  }

  /**
   * Substitutes long forms for all cached shortened URLs found in text, and issues requests for any 
   * not in the cache.
   */
  def getExpandedUrls(text: String, provideExpandedUrl: (String, String) => Unit) = {
    val matcher = LinkExtractor.hyperlinkPattern.matcher(text)
    while (matcher.find) {
      val url = matcher.group(1)
      if (urlIsShortened(url)) {
        fetcher.getCachedObject(url) match {
          case Some(longUrl) => provideExpandedUrl(url, longUrl)
          case None => fetcher.requestItem(new FetchRequest(url, url, 
            (urlReady: LongUrlReady) => {
              provideExpandedUrl(urlReady.key, urlReady.resource)
            }))
        }
      }
    }
  }
  
  private def urlIsShortened(url: String) = shortenerDomains.exists(url.contains(_))
  
}