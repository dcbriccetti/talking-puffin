package org.talkingpuffin.util

import redis.clients.jedis.JedisPool
import org.apache.commons.pool.impl.GenericObjectPool.Config
import java.util.concurrent.ConcurrentHashMap

object UrlsCache extends Loggable {
  val r = """.*"port":(\d+).*"hostname":"(.*?)".*"password":"(.*?)".*""".r

  def apply(): UrlsCache = {
    Option(System.getProperty("local.redis")) match {
      case Some(local) => new RedisUrlsCache(new JedisPool(new Config(), "localhost"))
      case None =>
        Option(System.getenv("VMC_SERVICES")) match {
          case Some(services) =>
            info(services)
            services match {
              case r(port, hostname, password) => {
                info("Creating JedisPool for " + hostname + ":" + port)
                new RedisUrlsCache(new JedisPool(new Config(), hostname, Integer.parseInt(port), 2000, password))
              }
            }
          case None => new TrivialUrlsCache
        }
      }
  }
}

abstract class UrlsCache extends Loggable {
  def get(urlString: String): Option[String]
  def put(shortUrl: String, longUrl: String)
}

class RedisUrlsCache(pool: JedisPool) extends UrlsCache {

  def get(urlString: String): Option[String] = {
    val jedis = pool.getResource
    val longUrlOp = Option(jedis.get(urlString))
    pool.returnResource(jedis)
    if (longUrlOp.isDefined) {
      debug("In cache: " + urlString + " -> " + longUrlOp.get)
    }
    longUrlOp
  }

  def put(shortUrl: String, longUrl: String) {
    val jedis = pool.getResource
    jedis.set(shortUrl, longUrl)
    jedis.expire(shortUrl, 60 * 60 * 12)
    pool.returnResource(jedis)
    debug(shortUrl + " -> " + longUrl)
  }
}

class TrivialUrlsCache extends UrlsCache {
  private val expandedUrls = new ConcurrentHashMap[String,String]

  def get(urlString: String): Option[String] = {
    val longUrl = expandedUrls.get(urlString)
    if (longUrl != null) {
      debug("In cache: " + urlString + " -> " + longUrl)
    }
    Option(longUrl)
  }

  def put(shortUrl: String, longUrl: String) {
    if (expandedUrls.size > 10000) {
      debug("Cache reached limit. Clearing.")
      expandedUrls.clear() // TODO replace with LRU
    }
    expandedUrls.put(shortUrl, longUrl)
    debug(shortUrl + " -> " + longUrl + " (" + expandedUrls.size + " in cache)")
  }
}
