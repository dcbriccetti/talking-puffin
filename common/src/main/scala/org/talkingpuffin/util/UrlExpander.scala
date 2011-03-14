package org.talkingpuffin.util

import java.net.{HttpURLConnection, URL}

/**
 * URL shortening and expanding.
 */
object UrlExpander extends Loggable {
  class NoRedirection(msg: String) extends Exception(msg)
  private val redirectionCodes = List(301, 302)

  def expand(urlString: String): String = {
    debug("Connecting to " + urlString)
    val url = new URL(urlString)
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setConnectTimeout(5000)
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
      throw new NoRedirection(urlString)
    }
  }
  
}

