package org.talkingpuffin.ui.util
import cache.Cache
import java.util.concurrent.{Executors, LinkedBlockingQueue}
import com.google.common.collect.MapMaker
import java.util.{Collections, HashSet}
import talkingpuffin.util.Loggable

/**
 * Fetches resources in the background, and calls a function in the Swing event thread when ready.
 */
abstract class BackgroundResourceFetcher[K,V](processResource: (ResourceReady[K,V]) => Unit) 
    extends Cache[K,V] with Loggable {
  val cache: java.util.Map[K,V] = new MapMaker().softValues().makeMap()
  val requestQueue = new LinkedBlockingQueue[FetchRequest[K]]
  val inProgress = Collections.synchronizedSet(new HashSet[K])
  val threadPool = Executors.newFixedThreadPool(15)
  
  new Thread(new Runnable {def run = while(true) processNextRequestWithPoolThread}).start

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
  def requestItem(request: FetchRequest[K]) = 
    if (! cache.containsKey(request.key) && 
        ! requestQueue.contains(request) && ! inProgress.contains(request.key)) 
      requestQueue.put(request)
  
  private def processNextRequestWithPoolThread {
    val fetchRequest = requestQueue.take
    val key = fetchRequest.key
    inProgress.add(key)
    
    threadPool.execute(new Runnable {
      def run {
        try {
          var resource = cache.get(key)
          if (resource == null) {
            resource = getResourceFromSource(key)
            debug("Got " + key)
            store(cache, key, resource)
          }
          
          SwingInvoke.later({processResource(new ResourceReady[K,V](key, fetchRequest.userData, resource))})
        } catch {
          case e: NoSuchResource => // Do nothing
        }
        inProgress.remove(key)
      }
    })
  }
  
  protected def getResourceFromSource(key: K): V
}

case class FetchRequest[K](val key: K, val userData: Object)

class ResourceReady[K,V](val key: K, val userData: Object, val resource: V)

case class NoSuchResource(resource: String) extends Exception
