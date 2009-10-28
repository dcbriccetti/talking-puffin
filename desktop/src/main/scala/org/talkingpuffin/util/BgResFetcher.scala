package org.talkingpuffin.util

import java.util.concurrent.{Future, Callable, Executors}
import com.google.common.collect.MapMaker
import javax.management.ObjectName
import management.ManagementFactory
import org.apache.log4j.Logger

trait BgResFetcherMBean {
  def getCacheSize: Int
  def getCacheHits: Int
  def getCacheMisses: Int
}

abstract class BgResFetcher[K,V](val resourceName: String) extends BgResFetcherMBean {

  val fetcherName = resourceName + " fetcher"
  val log = Logger.getLogger(fetcherName)
  val threadPool = Executors.newFixedThreadPool(15)
  val cache: java.util.Map[K,V] = new MapMaker().softValues().makeMap()
  var hits = 0
  var misses = 0

  val mbs = ManagementFactory.getPlatformMBeanServer
  mbs.registerMBean(this, new ObjectName("TalkingPuffin:name=" + fetcherName))

  def getCacheSize = cache.size
  def getCacheHits = hits
  def getCacheMisses = misses

  /**
   * Requests that an item be fetched in a background thread. If the key is already in the 
   * cache, the request is ignored. 
   */
  def requestItem(request: BgFetchRequest[K,V]): Option[Future[Unit]] = 
    if (cache.containsKey(request.key)) {
      log.debug(request.key.toString + " found in cache")
      provideResult(request, cache.get(request.key))
      None
    } else {
      Some(threadPool.submit(new Callable[Unit] {
        def call = {
          val value = {
            if (cache.containsKey(request.key)) {
              log.debug(request.key.toString + " found in cache")
              hits += 1
              cache.get(request.key)
            } else {
              log.debug("Getting value for " + request.key)
              misses += 1
              val newVal = getResourceFromSource(request.key)
              cache.put(request.key, newVal)
              newVal
            }
          }
          provideResult(request, value)
        }
      }))
    }
            
  private def provideResult[K,V](request: BgFetchRequest[K,V], value: V) = 
    request.processResource(new BgResourceReady(request.key, request.userData, value))

  protected def getResourceFromSource(key: K): V
  
}

case class BgFetchRequest[K,V](val key: K, val userData: Option[Any], 
    processResource: (BgResourceReady[K,V]) => Unit)

class BgResourceReady[K,V](val key: K, val userData: Option[Any], val resource: V)

case class BgNoSuchResource(resource: String) extends Exception
