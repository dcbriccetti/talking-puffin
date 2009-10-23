package org.talkingpuffin.twitter

import management.ManagementFactory
import javax.management.ObjectName
import org.apache.log4j.Logger

trait HttpRunnerMBean {
  /** A request is a single HTTP request, counted only once even if retries are necessary. */
  def getRequests: Long
  /** Counts of if and when the requests eventually succeeded */
  def getWhenSucceeded: Array[Long]
}

/**
 * Runs HTTP requests, retrying repeatedly if needed, recording when the request is 
 * ultimately successful, making those statistics available via Java Management 
 * Extensions (JMX).
 */
class HttpRunner(retryAfterFailureDelays: List[Int]) extends HttpRunnerMBean {
  private val log = Logger.getLogger("Http")
  private var requests = 0L
  private var delaysBefore = 0 :: retryAfterFailureDelays 
  private val whenSucceeded = new Array[Long](delaysBefore.length + 1 /* No more retries */)
  
  def getRequests = requests
  def getWhenSucceeded = whenSucceeded

  ManagementFactory.getPlatformMBeanServer.registerMBean(this, 
      new ObjectName("TalkingPuffin:name=HttpRunner"))

  def run[T](twitterOperation: => T): T = {
    requests += 1
    var lastException: TwitterException = null
    val lastIndex = whenSucceeded.length - 1
    
    0 until lastIndex foreach(i => {
      val delayMs = delaysBefore(i)
      if (delayMs > 0) {
        log.warn("Delaying " + delayMs + " ms")
        Thread.sleep(delayMs)
      }
      try {
        val result = twitterOperation
        whenSucceeded(i) += 1
        return result
      } catch {
        case e: TwitterException if e.code < 500 => throw e
        case e: TwitterException => {
          log.warn(e.toString)
          lastException = e
        }
      }
    })
    log.error("Giving up after " + delaysBefore.length + " attempts")
    whenSucceeded(lastIndex) += 1
    throw lastException
  }
}
