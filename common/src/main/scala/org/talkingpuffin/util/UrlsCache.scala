package org.talkingpuffin.util

import java.util.concurrent.ConcurrentHashMap
import org.joda.time.DateTime

class UrlsCache extends Loggable {
  private val expandedUrls = new ConcurrentHashMap[String,CachedExpandedUrl]

  def get(urlString: String): Option[CachedExpandedUrl] = {
    val ceu = expandedUrls.get(urlString)
    if (ceu != null) {
      debug("In cache: " + urlString + " -> " + ceu.url)
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
