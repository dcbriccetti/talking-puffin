package org.talkingpuffin.util

import java.net.{HttpURLConnection, URL}
import java.text.NumberFormat

/**
 * URL shortening and expanding.
 */
object UrlExpander extends Loggable {
  class NoRedirection(msg: String) extends Exception(msg)
  private val redirectionCodes = List(301, 302)
  private val cache = new UrlsCache
  private val fmt = NumberFormat.getInstance

  def expand(urlString: String): String = {
    val url = new URL(urlString)

    def process: String = {
      if (url.getProtocol == "https")
        return urlString

      cache.get(urlString).foreach(expUrl => return expUrl.url.getOrElse(urlString))

      getRedirectionChain(url) match {
        case Nil =>
          debug(urlString + " does not redirect anywhere")
          cache.put(urlString, None)
          throw new NoRedirection(urlString)
        case ultimateUrl :: others =>
          val expandedUrl = ultimateUrl.toString
          cache.put(urlString, Some(expandedUrl))
          expandedUrl
      }
    }

    val startTime = System.currentTimeMillis
    val result = process
    debug(urlString + " in " + fmt.format(System.currentTimeMillis - startTime) + " ms")
    result
  }

  private def getRedirectionChain(url: URL): List[URL] = {
    val conn = getConnection(url)
    if (isRedir(conn)) {
      val nextUrl = new URL(getLocFromHeader(conn, url))
      if (nextUrl.getProtocol == "https")
        nextUrl :: url :: Nil
      else
        getRedirectionChain(nextUrl) ++ List(url)
    } else
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
    if (locHeader.startsWith("/"))
      (new URL(url.getProtocol, url.getHost, locHeader)).toString
    else
      locHeader
  }

  private def isRedir(conn: HttpURLConnection) = redirectionCodes.contains(conn.getResponseCode)
}
