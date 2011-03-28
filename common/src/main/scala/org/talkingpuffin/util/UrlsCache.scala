package org.talkingpuffin.util

import redis.clients.jedis.JedisPool
import org.apache.commons.pool.impl.GenericObjectPool.Config

class UrlsCache extends Loggable {
  val r = """.*"port":(\d+).*"hostname":"(.*?)".*"password":"(.*?)".*""".r

  val pool = Option(System.getenv("VMC_SERVICES")) match {
    case Some(services) =>
      info(services)
      services match {
        case r(port, hostname, password) => {
          info("Creating JedisPool for " + hostname + ":" + port)
          new JedisPool(new Config(), hostname, Integer.parseInt(port), 2000, password)
        }
      }
    case None =>
      info("Creating JedisPool using defaults")
      new JedisPool(new Config(), "localhost")
  }

  def get(urlString: String): Option[String] = {
    val jedis = pool.getResource
    val ceu = Option(jedis.get(urlString))
    pool.returnResource(jedis)
    if (ceu.isDefined) {
      debug("In cache: " + urlString + " -> " + ceu.get)
    }
    ceu
  }

  def put(shortUrl: String, longUrl: String) = {
    val jedis = pool.getResource
    jedis.set(shortUrl, longUrl)
    jedis.expire(shortUrl, 60 * 60 * 12)
    pool.returnResource(jedis)
    debug(shortUrl + " -> " + longUrl)
  }
}
