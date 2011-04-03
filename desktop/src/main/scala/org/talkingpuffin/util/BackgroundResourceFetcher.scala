package org.talkingpuffin.util
import java.io._
import java.util.concurrent._
import java.util.{Collections, HashSet}
import scala.collection.JavaConversions._
import management.ManagementFactory
import javax.management.ObjectName
import org.apache.log4j.Logger
import com.redis.serialization.Parse.Implicits._

trait BackgroundResourceFetcherMBean {
  def getCacheSize: Int
  def getCacheHits: Int
  def getCacheMisses: Int
}

/**
 * Fetches resources in the background, and calls a function in the Swing event thread when ready.
 */
abstract class BackgroundResourceFetcher[T <: Serializable](resourceName: String, numThreads: Int = 10,
    waitingLimit: Option[Int] = None) extends BackgroundResourceFetcherMBean {

  private val fetcherName = resourceName + " fetcher"
  private val log = Logger.getLogger(fetcherName)
  private val cache = Cache[Array[Byte]](fetcherName)
  private val inProgress = Collections.synchronizedSet(new HashSet[String])
  private case class RunnableFetch(key: String) extends Runnable { def run() {}}
  private val runnableQueue = new LinkedBlockingDeque[Runnable] { override def take() = super.takeLast() }
  private val threadPool = new ThreadPoolExecutor(numThreads, numThreads, 30, TimeUnit.SECONDS,
    runnableQueue, new NamedThreadFactory(resourceName))
  private var hits = 0
  private var misses = 0

  private val mbs = ManagementFactory.getPlatformMBeanServer
  mbs.registerMBean(this, new ObjectName("TalkingPuffin:name=" + fetcherName))

  def getCacheSize = cache.size
  def getCacheHits = hits
  def getCacheMisses = misses

  /**
   * Returns the object if it exists in the cache, otherwise None.
   */
  def getCachedObject(key: String): Option[T] = {
    try {
      cache.get(key) match {
        case Some(obj) =>
          hits += 1
          Some(Serializer.deSerialize(obj))
        case None =>
          misses += 1
          None
      }
    } catch {
      case ex: Throwable => {
        error(ex.toString)
        error(ex.getStackTraceString)
      }
    }
  }

  /**
   * Requests that an item be fetched in a background thread. If the key is already in the 
   * cache, the request is ignored. 
   */
  def requestItem(request: FetchRequest[T]) =
    if (! cache.get(request.key).isDefined && !inProgress.contains(request.key)) {
      waitingLimit.foreach(limit =>
        while (runnableQueue.size > limit - 1) {
          val runnableFetch = runnableQueue.takeFirst.asInstanceOf[RunnableFetch]
          inProgress.remove(runnableFetch.key)
          log.debug("Removed old fetch request for " + runnableFetch.key)
        }
      )
      submitRequestAsRunnable(request)
    }
  
  /**
   * Gets the item from the cache if present and calls the callback in the same thread, or
   * submits a request for the item.
   */
  def get(provideValue: (T) => Unit)(key: String): Unit = {
    getCachedObject(key) match {
      case Some(value) => provideValue(value)
      case None => requestItem(FetchRequest(key, null, (urlReady: ResourceReady[T]) => {
          provideValue(urlReady.resource)
        }))
    }
  }
  
  protected def getResourceFromSource(key: String): T

  private def submitRequestAsRunnable(fetchRequest: FetchRequest[T]) {
    val key = fetchRequest.key
    log.debug("Enqueueing Runnable. Now contains " +
      runnableQueue.toList.map(_.asInstanceOf[RunnableFetch]).map(_.key).mkString(", ") + ".")
    inProgress.add(key)
    threadPool.execute(new RunnableFetch(key) { override def run() { processFetchRequest(fetchRequest) }})
  }

  private def processFetchRequest(fetchRequest: FetchRequest[T]) {
    log.debug("Runnable queue size: " + runnableQueue.size)
    val key = fetchRequest.key
    try {
      val resource = cache.get(key) match {
        case Some(res: Array[Byte]) => Serializer.deSerialize(res)
        case None =>
          val res = getResourceFromSource(key)
          cache.put(key, Serializer.serialize(res))
          res
      }

      fetchRequest.returnResult(new ResourceReady[T](key, fetchRequest.userData, resource))
    } catch {
      case e: NoSuchResource => // Do nothing
    }
    inProgress.remove(key)
  }

}

case class FetchRequest[T](key: String, userData: Object, returnResult: (ResourceReady[T]) => Unit)

class ResourceReady[T](val key: String, val userData: Object, val resource: T)

case class NoSuchResource(resource: String) extends Exception
