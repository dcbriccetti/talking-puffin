package org.talkingpuffin.ui.util

import java.net.{HttpURLConnection, URL}
import org.talkingpuffin.ui.LinkExtractor
import org.talkingpuffin.util.Loggable

/**
 * URL shortening and expanding.
 */
object ShortUrl extends Loggable {
  val shortenerDomains = List("bit.ly", "digg.com", "ff.im", "is.gd", "ping.fm", "short.ie", "su.pr", 
    "tinyurl.com", "tr.im")
  val regex = "http://(" + shortenerDomains.map(_.replace(".","""\.""")).mkString("|") + ")/" + 
      LinkExtractor.urlCharClass + "*"
  private val redirectionCodes = List(301, 302)
  private type LongUrlReady = ResourceReady[String,String]
  
  private val fetcher = new BackgroundResourceFetcher[String, String] {
    override def getResourceFromSource(urlString: String): String = {
      debug("Connecting to " + urlString)
      val url = new URL(urlString)
      val conn = url.openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestMethod("HEAD")
      conn.setInstanceFollowRedirects(false)
      conn.setRequestProperty("User-agent", "TalkingPuffin")
      if (redirectionCodes.contains(conn.getResponseCode)) {
        val loc = {
          val locHeader = conn.getHeaderField("Location")
          if (locHeader.startsWith("/")) (new URL(url.getProtocol, url.getHost, locHeader)).toString else locHeader
        }
        debug("Redirected to " + loc)
        loc
      } else {
        debug("No such")
        throw new NoSuchResource(urlString)
      }
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
      debug(url)
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