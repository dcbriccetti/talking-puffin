package org.talkingpuffin.util

import java.net.{HttpURLConnection, URL}
import org.talkingpuffin.ui.LinkExtractor

/**
 * URL shortening and expanding.
 */
object ShortUrl extends Loggable {
  private val shortenerRegexStrings = List("""http://digg\.com/""" + LinkExtractor.urlCharClass + "{4,10}")
  private val shortenerRegexes = shortenerRegexStrings.map(_.r)
  private val shortenerDomains = List("bit.ly", "ff.im", "is.gd", "ping.fm", "short.ie", "su.pr", 
    "tinyurl.com", "tr.im")
  private val regex = "http://(" + shortenerDomains.map(_.replace(".","""\.""")).mkString("|") + ")/" + 
      LinkExtractor.urlCharClass + "*"
  private val redirectionCodes = List(301, 302)
  private type LongUrlReady = BgResourceReady[String,String]
  
  private val fetcher = new BgResFetcher[String, String] {
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
        debug(urlString + " does not redirect anywhere")
        throw new BgNoSuchResource(urlString)
      }
    }
  }

  /**
   * If simply doing HTTP HEAD to get Location suffices to bypass the wrapper
   */
  def redirectionBypassesWrapper(host: String) = host == "su.pr"
  
  def substituteShortenedUrlWith(text: String, replacement: String) = {
    (regex :: shortenerRegexStrings).foldLeft(text)(_.replaceAll(_, replacement))
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
        fetcher.requestItem(new BgFetchRequest(url, None, (urlReady: LongUrlReady) => 
          provideExpandedUrl(urlReady.key, urlReady.resource)))
      }
    }
  }
  
  private def urlIsShortened(url: String) = shortenerDomains.exists(url.contains(_)) ||
    shortenerRegexes.exists(r => url match {case r() => true case _ => false})
  
}