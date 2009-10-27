package org.talkingpuffin.util

import java.util.concurrent.{Future, Callable, Executors}
import com.google.common.collect.MapMaker

abstract class BgResFetcher[K,V] extends Loggable {

  val threadPool = Executors.newFixedThreadPool(15)
  val cache: java.util.Map[K,V] = new MapMaker().softValues().makeMap()

  /**
   * Requests that an item be fetched in a background thread. If the key is already in the 
   * cache, the request is ignored. 
   */
  def requestItem(request: BgFetchRequest[K,V]): Option[Future[Unit]] = 
    if (cache.containsKey(request.key)) {
      debug(request.key.toString + " found in cache")
      provideResult(request, cache.get(request.key))
      None
    } else {
      val future = threadPool.submit(new Callable[Unit] {
        def call = {
          val value = {
            if (cache.containsKey(request.key)) {
              debug(request.key.toString + " found in cache")
              cache.get(request.key)
            } else {
              debug(request.key.toString + ": getting value for")
              val newVal = getResourceFromSource(request.key)
              cache.put(request.key, newVal)
              newVal
            }
          }
          provideResult(request, value)
        }
      })
      Some(future)
    }
            
  private def provideResult[K,V](request: BgFetchRequest[K,V], value: V) = 
    request.processResource(new BgResourceReady(request.key, request.userData, value))

  protected def getResourceFromSource(key: K): V
  
}

case class BgFetchRequest[K,V](val key: K, val userData: Option[Any], 
    processResource: (BgResourceReady[K,V]) => Unit)

class BgResourceReady[K,V](val key: K, val userData: Object, val resource: V)

case class BgNoSuchResource(resource: String) extends Exception
