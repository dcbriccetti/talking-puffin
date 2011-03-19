package org.talkingpuffin.util

import java.net.{HttpURLConnection, URL}
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.DateTime

/**
 * URL shortening and expanding.
 */
object UrlExpander extends Loggable {
  class NoRedirection(msg: String) extends Exception(msg)
  private val redirectionCodes = List(301, 302)
  private val cache = new UrlsCache

  def expand(urlString: String): String = {
    val url = new URL(urlString)

    if (url.getProtocol == "https")
      return urlString

    cache.get(urlString).foreach(ceu => {
      debug("In cache: " + urlString + " -> " + ceu.url)
      return ceu.url.getOrElse(urlString)
    })

    getRedirectionChain(url) match {
      case Nil =>
        debug(urlString + " does not redirect anywhere")
        cache.put(urlString, None)
        throw new NoRedirection(urlString)
      case ultimateUrl :: others =>
        val loc = ultimateUrl.toString
        cache.put(urlString, Some(loc))
        loc
    }
  }

  private def getRedirectionChain(url: URL): List[URL] = {
    val conn = getConnection(url)
    if (isRedir(conn))
      getRedirectionChain(new URL(getLocFromHeader(conn, url))) ++ List(url)
    else
      List(url)
  }

  private def getConnection(url: URL) = {
    debug("Connecting to " + url)
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setConnectTimeout(5000)
    conn.setRequestMethod("HEAD")
    conn.setInstanceFollowRedirects(false)
    conn.setRequestProperty("User-agent", "TalkingPuffin")
    conn
  }

  private def getLocFromHeader(conn: HttpURLConnection, url: URL): String = {
    val locHeader = conn.getHeaderField("Location")
    val loc = if (locHeader.startsWith("/")) (new URL(url.getProtocol, url.getHost, locHeader)).toString else locHeader
    loc
  }

  private def isRedir(conn: HttpURLConnection) = redirectionCodes.contains(conn.getResponseCode)

}

class UrlsCache extends Loggable {
  private val expandedUrls = new ConcurrentHashMap[String,CachedExpandedUrl]

  def get(urlString: String): Option[CachedExpandedUrl] = {
    val ceu = expandedUrls.get(urlString)
    if (ceu != null) {
      put(urlString, ceu.url) // Update date
    }
    Option(ceu)
  }

  def put(shortUrl: String, longUrl: Option[String]) = {
    if (expandedUrls.size > 10000) {
      debug("Cache reached limit. Clearing.")
      expandedUrls.clear // TODO replace with LRU
    }
    expandedUrls.put(shortUrl, CachedExpandedUrl(new DateTime, longUrl))
    debug(shortUrl + " -> " + longUrl + " (" + expandedUrls.size + " in cache)")
  }
}

case class CachedExpandedUrl(lastUsed: DateTime, url: Option[String])
