package com.davebsoft.sctw.ui.util
import cache.Cache
import java.util.concurrent.{ConcurrentHashMap, Executors, LinkedBlockingQueue}
import google.common.collect.MapMaker
import java.util.{Collections, HashSet}
import org.apache.log4j.Logger

case class FetchRequest[K](val key: K, val userData: Object)

class ResourceReady[K,V](val key: K, val id: Object, val resource: V)

/**
 * Fetches resources in the background, and calls a function in the Swing event thread when ready.
 * 
 * @author Dave Briccetti
 */
abstract class BackgroundResourceFetcher[K,V](processReadyResource: (ResourceReady[K,V]) => Unit) 
    extends Cache[K,V] {
  val log = Logger.getLogger(getClass)
  val cache: java.util.Map[K,V] = new MapMaker().softValues().makeMap()
  val requestQueue = new LinkedBlockingQueue[FetchRequest[K]]
  val inProgress = Collections.synchronizedSet(new HashSet[K])
  val threadPool = Executors.newFixedThreadPool(15)
  
  new Thread(new Runnable {
    def run = while(true) processNextRequestWithPoolThread
  }).start

  private def processNextRequestWithPoolThread {
    val fetchRequest = requestQueue.take
    val key = fetchRequest.key
    inProgress.add(key)
    
    threadPool.execute(new Runnable {
      def run = {
        var resource = cache.get(key)
        if (resource == null) {
          resource = getResourceFromSource(key)
          log.debug("Fetched from source: " + key)
          store(cache, key, resource)
        }
        inProgress.remove(key)
        
        SwingInvoke.invokeLater({
          processReadyResource(new ResourceReady[K,V](key, fetchRequest.userData, resource))
        })
      }
    })
  }
  
  /**
   * Returns the object if it exists in the cache, otherwise None.
   */
  def getCachedObject(key: K): Option[V] = {
    val obj = cache.get(key)
    if (obj != null) Some(obj) else None
  }

  /**
   * Requests that an item be fetched in a background thread. If the key is already in the 
   * cache, the request is ignored. 
   */
  def requestItem(request: FetchRequest[K]) {
    if (! cache.containsKey(request.key)) {
      if (! requestQueue.contains(request) && ! inProgress.contains(request.key)) {
        requestQueue.put(request)
      }
    }
  }
  
  protected def getResourceFromSource(key: K): V
}

