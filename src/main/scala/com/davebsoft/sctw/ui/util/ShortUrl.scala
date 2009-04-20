package com.davebsoft.sctw.ui.util

import java.net.{HttpURLConnection, URL}

/**
 * URL shortening and expanding.
 * @author Dave Briccetti
 */
object ShortUrl {
  val domains = List("bit.ly", "ff.im", "is.gd", "tinyurl.com", "tr.im")
  val redirectionCodes = List(301, 302)
  
  def expand(url: String): Option[String] = {
    val conn = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("HEAD")
    conn.setInstanceFollowRedirects(false)
    if (redirectionCodes.contains(conn.getResponseCode)) Some(conn.getHeaderField("Location")) else None
  }
}