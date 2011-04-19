package org.talkingpuffin.util

import java.util.concurrent.ConcurrentHashMap
import com.redis.{RedisClient, RedisClientPool}
import com.redis.serialization._
import com.redis.serialization.Parse._

object Cache extends Loggable {
  val r = """.*"hostname":"(.*?)".*"port":(\d+).*"password":"(.*?)".*""".r

  def apply[T](superKey: Option[String])(implicit format: Format, parse: Parse[T]): Cache[T] = {
    Option(System.getProperty("local.redis")) match {
      case Some(local) => new RedisCache[T](new RedisClientPool("localhost", 6379),
        None, superKey)(format, parse)
      case None =>
        Option(System.getenv("VMC_SERVICES")) match {
          case Some(services) =>
            info(services)
            services match {
              case r(hostname, port, password) => {
                info("Creating RedisClientPool for " + hostname + ":" + port)
                new RedisCache[T](new RedisClientPool(hostname, Integer.parseInt(port)),
                  Some(password), superKey)(format, parse)
              }
            }
          case None => new TrivialCache[T]
        }
      }
  }
  
  def apply[T](superKey: String)(implicit format: Format, parse: Parse[T]): Cache[T] =
    apply(Some(superKey))(format, parse)
}

abstract class Cache[T](pool: RedisClientPool, superKey: Option[String]) extends Loggable {
  def get(urlString: String): Option[T]
  def put(shortUrl: String, longUrl: T, expiration: Int = 0)
  def size: Int
}

class RedisCache[T](pool: RedisClientPool, password: Option[String],
                    superKey: Option[String])(format: Format, parse: Parse[T])
  extends Cache[T](pool, superKey) {

  def get(partialKey: String): Option[T] = {
    val key = makeKey(partialKey)
    val valueOp: Option[T] = pool.withClient((client: RedisClient) => {
      password.foreach(pw => client.auth(pw))
      debug("Getting " + key)
      try {
        client.get[T](key)(format, parse)
      } catch {
        case ex: Throwable =>
          error(ex.toString)
          error(ex.getStackTraceString)
          None
      }
    })
    if (valueOp.isDefined) {
      debug("In cache: " + key + " -> " + valueOp.get)
    }
    valueOp
  }

  def put(partialKey: String, value: T, expiration: Int = 60 * 60 * 12) {
    val key = makeKey(partialKey)
    pool.withClient((client: RedisClient) => {
      client.set(key, value)
      client.expire(key, expiration)
    })
    debug(key + " -> " + value)
  }

  def size: Int =
    pool.withClient((client: RedisClient) => {
      client.dbsize.getOrElse(0)
    })

  private def makeKey(key: String) = superKey match {
    case Some(skey) => skey + ":" + key
    case None => key
  }
}

class TrivialCache[T] extends Cache[T](null, null) {
  private val expandedUrls = new ConcurrentHashMap[String,T]

  def get(urlString: String): Option[T] = Option(expandedUrls.get(urlString))

  def put(shortUrl: String, longUrl: T, expiration: Int = 0) {
    if (expandedUrls.size > 10000) {
      debug("Cache reached limit. Clearing.")
      expandedUrls.clear() // TODO replace with LRU
    }
    expandedUrls.put(shortUrl, longUrl)
    debug(shortUrl + " -> " + longUrl + " (" + expandedUrls.size + " in cache)")
  }

  def size: Int = expandedUrls.size
}
