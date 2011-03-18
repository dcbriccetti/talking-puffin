package org.talkingpuffin.util

import java.net.{HttpURLConnection, URL}
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.DateTime

/**
 * URL shortening and expanding.
 */
object UrlExpander extends Loggable {
  class NoRedirection(msg: String) extends Exception(msg)
  private case class CachedExpandedUrl(lastUsed: DateTime, url: Option[String])
  private val redirectionCodes = List(301, 302)
  private val expandedUrls = new ConcurrentHashMap[String,CachedExpandedUrl]

  def expand(urlString: String): String = {
    val url = new URL(urlString)

    if (url.getProtocol == "https")
      return urlString

    expandedUrls.get(urlString) match {
      case ceu if ceu == null =>
      case ceu: CachedExpandedUrl =>
        debug("In cache: " + urlString + " -> " + ceu.url)
        expandedUrls.put(urlString, CachedExpandedUrl(new DateTime, ceu.url)) // Update DateTime
        return ceu.url.getOrElse(urlString)
    }

    getNewLoc(url) match {
      case Some(loc) =>
        expandedUrls.put(urlString, CachedExpandedUrl(new DateTime, Some(loc)))
        debug(urlString + " -> " + loc + " (" + expandedUrls.size + " in cache)")
        loc
      case None =>
        debug(urlString + " does not redirect anywhere")
        if (expandedUrls.size > 10000) {
          debug("Cache reached limit. Clearing.")
          expandedUrls.clear // TODO replace with LRU
        }
        expandedUrls.put(urlString, CachedExpandedUrl(new DateTime, None))
        throw new NoRedirection(urlString)
    }
  }

  private def getNewLoc(url: URL): Option[String] = {
    debug("Connecting to " + url)
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setConnectTimeout(5000)
    conn.setRequestMethod("HEAD")
    conn.setInstanceFollowRedirects(false)
    conn.setRequestProperty("User-agent", "TalkingPuffin")
    if (redirectionCodes.contains(conn.getResponseCode)) {
      val locHeader = conn.getHeaderField("Location")
      Some(if (locHeader.startsWith("/")) (new URL(url.getProtocol, url.getHost, locHeader)).toString else locHeader)
    } else {
      None
    }
  }

}
