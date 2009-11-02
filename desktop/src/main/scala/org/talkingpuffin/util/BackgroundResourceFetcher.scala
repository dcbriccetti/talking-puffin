package org.talkingpuffin.util
import java.util.concurrent.{Executors, LinkedBlockingQueue}
import com.google.common.collect.MapMaker
import java.util.{Collections, HashSet}
import org.talkingpuffin.ui.SwingInvoke
import management.ManagementFactory
import javax.management.ObjectName
import org.apache.log4j.Logger

trait BackgroundResourceFetcherMBean {
  def getCacheSize: Int
  def getCacheHits: Int
  def getCacheMisses: Int
}

/**
 * Fetches resources in the background, and calls a function in the Swing event thread when ready.
 */
abstract class BackgroundResourceFetcher[K,V](resourceName: String) 
    extends BackgroundResourceFetcherMBean {

  val fetcherName = resourceName + " fetcher"
  val log = Logger.getLogger(fetcherName)
  val cache: java.util.Map[K,V] = new MapMaker().softValues().makeMap()
  val requestQueue = new LinkedBlockingQueue[FetchRequest[K,V]]
  val inProgress = Collections.synchronizedSet(new HashSet[K])
  val threadPool = Executors.newCachedThreadPool
  var running = true
  var hits = 0
  var misses = 0

  val mbs = ManagementFactory.getPlatformMBeanServer
  mbs.registerMBean(this, new ObjectName("TalkingPuffin:name=" + fetcherName))
  def getCacheSize = cache.size
  def getCacheHits = hits
  def getCacheMisses = misses

  val thread = new Thread(new Runnable {
    def run = {
      while (running)
        try {
          val fetchRequest = requestQueue.take
          val key = fetchRequest.key
          inProgress.add(key)

          threadPool.execute(new Runnable {
            def run {
              try {
                var resource = cache.get(key)
                if (resource == null) {
                  resource = getResourceFromSource(key)
                  cache.put(key, resource)
                }

                SwingInvoke.later({fetchRequest.processResource(
                    new ResourceReady[K,V](key, fetchRequest.userData, resource))})
              } catch {
                case e: NoSuchResource => // Do nothing
              }
              inProgress.remove(key)
            }
          })
        } catch {
          case e: InterruptedException => // This is how the thread is ended
        }
    }
  }, getClass.getName + " " + hashCode)
  thread.start

  /**
   * Returns the object if it exists in the cache, otherwise None.
   */
  def getCachedObject(key: K): Option[V] = {
    val obj = cache.get(key)
    if (obj != null) hits += 1 else misses += 1
    if (obj != null) Some(obj) else None
  }

  /**
   * Requests that an item be fetched in a background thread. If the key is already in the 
   * cache, the request is ignored. 
   */
  def requestItem(request: FetchRequest[K,V]) = 
    if (! cache.containsKey(request.key) && 
        ! requestQueue.contains(request) && ! inProgress.contains(request.key)) 
      requestQueue.put(request)
  
  /**
   * Gets the item from the cache if present and calls the callback in the same thread, or
   * submits a request for the item.
   */
  def get(provideExpandedUrl: (V) => Unit)(url: K): Unit = {
    getCachedObject(url) match {
      case Some(longUrl) => provideExpandedUrl(longUrl)
      case None => requestItem(new FetchRequest(url, null, 
        (urlReady: ResourceReady[K,V]) => {
          provideExpandedUrl(urlReady.resource)
        }))
    }
  }
  
  def stop = {
    if (running) {
      running = false
      threadPool.shutdownNow
      thread.interrupt
    }
  }
  
  protected def getResourceFromSource(key: K): V
}

case class FetchRequest[K,V](val key: K, val userData: Object, processResource: (ResourceReady[K,V]) => Unit)

class ResourceReady[K,V](val key: K, val userData: Object, val resource: V)

case class NoSuchResource(resource: String) extends Exception
