package org.talkingpuffin.util
import java.util.{Collections, HashSet}
import org.talkingpuffin.ui.SwingInvoke
import management.ManagementFactory
import javax.management.ObjectName
import org.apache.log4j.Logger
import org.talkingpuffin.ui.util.Threads
import java.util.concurrent.atomic.AtomicBoolean
import java.io._
import com.redis.serialization._
import com.redis.serialization.Parse.Implicits._
import java.util.concurrent.{Executors, LinkedBlockingQueue}

trait BackgroundResourceFetcherMBean {
  def getCacheSize: Int
  def getCacheHits: Int
  def getCacheMisses: Int
}

/**
 * Fetches resources in the background, and calls a function in the Swing event thread when ready.
 */
abstract class BackgroundResourceFetcher[T <: Serializable](resourceName: String, numThreads: Int = 20)
    extends BackgroundResourceFetcherMBean {

  private val fetcherName = resourceName + " fetcher"
  private val log = Logger.getLogger(fetcherName)
  private val cache = Cache[Array[Byte]](fetcherName)
  private val requestQueue = new LinkedBlockingQueue[FetchRequest[T]]
  private val inProgress = Collections.synchronizedSet(new HashSet[String])
  private val threadPool = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory(resourceName))
  private val running = new AtomicBoolean(true)
  private var hits = 0
  private var misses = 0

  val mbs = ManagementFactory.getPlatformMBeanServer
  mbs.registerMBean(this, new ObjectName("TalkingPuffin:name=" + fetcherName))
  def getCacheSize = cache.size
  def getCacheHits = hits
  def getCacheMisses = misses

  Threads.pool.execute(new Runnable {
    def run() {
      while (running.get)
        try {
          val fetchRequest = requestQueue.take
          val key = fetchRequest.key
          inProgress.add(key)

          threadPool.execute(new Runnable {
            def run() {
              try {
                val resource = cache.get(key) match {
                  case Some(res: Array[Byte]) => deSerialize(res)
                  case None =>
                    val res = getResourceFromSource(key)
                    cache.put(key, serialize(res))
                    res
                }

                SwingInvoke.later{fetchRequest.processResource(
                    new ResourceReady[T](key, fetchRequest.userData, resource))}
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
  })

  /**
   * Returns the object if it exists in the cache, otherwise None.
   */
  def getCachedObject(key: String): Option[T] = {
    try {
      cache.get(key) match {
        case Some(obj) =>
          hits += 1
          Some(deSerialize(obj))
        case None =>
          misses += 1
          None
      }
    } catch {
      case ex: Exception => {
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
    if (! cache.get(request.key).isDefined &&
        ! requestQueue.contains(request) && ! inProgress.contains(request.key)) 
      requestQueue.put(request)
  
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
  
  def stop() {
    running.set(false)
  }
  
  protected def getResourceFromSource(key: String): T

  private def serialize(obj: AnyRef): Array[Byte] = {
    val os = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(os)
    oos.writeObject(obj)
    oos.close()
    os.close()
    os.toByteArray
  }

  private def deSerialize(stream: Array[Byte]): T = {
    val is = new ByteArrayInputStream(stream)
    val ois = new ObjectInputStream(is)
    ois.readObject.asInstanceOf[T]
  }
}

case class FetchRequest[T](key: String, userData: Object, processResource: (ResourceReady[T]) => Unit)

class ResourceReady[T](val key: String, val userData: Object, val resource: T)

case class NoSuchResource(resource: String) extends Exception
