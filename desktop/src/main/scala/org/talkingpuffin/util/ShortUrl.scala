package org.talkingpuffin.util

import java.net.{HttpURLConnection, URL}
import org.talkingpuffin.ui.LinkExtractor

/**
 * URL shortening and expanding.
 */
object ShortUrl extends Loggable {
  private val shortenerRegexStrings = List("""http://digg\.com/""" + LinkExtractor.urlCharClass + "{4,10}")
  private val shortenerRegexes = shortenerRegexStrings.map(_.r)
  private val redirBypassesWrapperHosts = List("su.pr", "ow.ly")
  private val shortenerDomains = List("bit.ly", "ff.im", "is.gd", "j.mp", "ping.fm", 
    "r2.ly", "short.ie", "su.pr", 
    "tinyurl.com", "tr.im", "goo.gl", "t.co", "huff.to", "scoble.it", "oreil.ly") ::: redirBypassesWrapperHosts
  private val regex = "http://(" + shortenerDomains.map(_.replace(".","""\.""")).mkString("|") + ")/" + 
      LinkExtractor.urlCharClass + "*"
  private val redirectionCodes = List(301, 302)
  private type LongUrlReady = ResourceReady[String,String]
  
  private val fetcher = new BackgroundResourceFetcher[String, String]("URL") {
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
        throw new NoSuchResource(urlString)
      }
    }
  }

  /**
   * If simply doing HTTP HEAD to get Location suffices to bypass the wrapper
   */
  def redirectionBypassesWrapper(host: String) = redirBypassesWrapperHosts contains host
  
  def substituteShortenedUrlWith(text: String, replacement: String) = {
    (regex :: shortenerRegexStrings).foldLeft(text)(_.replaceAll(_, replacement))
  }

  /**
   * Gets the long form, if there is one, for the specified URL.
   */
  def getExpandedUrl(url: String, provideExpandedUrl: (String) => Unit) = {
    if (urlIsShortened(url)) {
      fetcher.get(provideExpandedUrl)(url)
    }
  }
  
  /**
   * Gets the long forms, if they exist, for all cached shortened URLs found in text.
   */
  def getExpandedUrls(text: String, provideSourceAndTargetUrl: (String, String) => Unit) = {
    val matcher = LinkExtractor.hyperlinkPattern.matcher(text)
    while (matcher.find) {
      val sourceUrl = matcher.group(1)
      getExpandedUrl(sourceUrl, (targetUrl: String) => {provideSourceAndTargetUrl(sourceUrl, targetUrl)})
    }
  }
  
  private def urlIsShortened(url: String) = shortenerDomains.exists(url.contains(_)) ||
    shortenerRegexes.exists(r => url match {case r() => true case _ => false})
  
}