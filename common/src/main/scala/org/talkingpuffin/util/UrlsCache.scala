package org.talkingpuffin.util

import com.redis.RedisClient

class UrlsCache extends Loggable {
  val r = """.*"port":(\d+).*"hostname":"(.*?)".*"password":"(.*?)".*""".r

  val redis = Option(System.getenv("VMC_SERVICES")) match {
    case Some(services) =>
      info(services)
      services match {
        case r(port, hostname, password) => {
          info("Creating RedisClient for " + hostname + ":" + port)
          new RedisClient(hostname, Integer.parseInt(port)) { auth(password) }
        }
      }
    case None =>
      info("Creating RedisClient using defaults")
      new RedisClient
  }

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
