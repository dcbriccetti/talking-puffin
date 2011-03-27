package org.talkingpuffin.util

import com.redis.RedisClient

class UrlsCache extends Loggable {
  val redis = new RedisClient

  def get(urlString: String): Option[String] = {
    val ceu = redis.synchronized { redis.get(urlString) }
    if (ceu.isDefined) {
      debug("In cache: " + urlString + " -> " + ceu.get)
    }
    ceu
  }

  def put(shortUrl: String, longUrl: String) = {
    redis.synchronized {
      redis.set(shortUrl, longUrl)
      redis.expire(shortUrl, 60 * 60 * 12)
    }
    debug(shortUrl + " -> " + longUrl)
  }
}
