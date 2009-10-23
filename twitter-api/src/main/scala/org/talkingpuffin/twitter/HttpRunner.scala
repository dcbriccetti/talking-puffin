package org.talkingpuffin.twitter

import management.ManagementFactory
import javax.management.ObjectName
import org.apache.log4j.Logger

trait HttpRunnerMBean {
  /** A request is a single HTTP request, counted only once even if retries are necessary. */
  def getRequests: Long
  /** A failure is an HTTP request that failed. There can be >= 0 failures for one request. */
  def getFailures: Long
  /** Returns a string containing all the retry statistics */
  def getWhenSucceeded: String
}

/**
 * Runs HTTP requests, retrying repeatedly if need, recording when the request is 
 * ultimately successful, making thoso statistics available via Java Management 
 * Extensions (JMX).
 */
class HttpRunner extends HttpRunnerMBean {
  private val log = Logger.getLogger("Http")
  private var requests = 0L
  def getRequests = requests
  private var failures = 0
  def getFailures = failures
  val retryDelays = List(0, 250, 2000, 2000, 5000, 10000, 60000)
  val maxRetries = retryDelays.length
  val whenSucceeded = new Array[Long](maxRetries + 1)
  def getWhenSucceeded: String = (List("1st try") ::: retryDelays).zip(whenSucceeded.toList).
      map(p => p._1.toString + ": " + p._2).mkString(", ")

  ManagementFactory.getPlatformMBeanServer.registerMBean(this, new ObjectName("TalkingPuffin:name=HttpRunner"))

  def run[T](twitterOperation: => T): T = {
    var lastException: TwitterException = null
    requests += 1

    retryDelays.zipWithIndex.foreach(delayWithIndex => {
      val delayMs = delayWithIndex._1
      val repeatCount = delayWithIndex._2

      try {
        val result = twitterOperation
        whenSucceeded(repeatCount) += 1
        return result
      } catch {
        case e: TwitterException if e.code < 500 => throw e
        case e: TwitterException => {
          failures += 1
          log.warn(e.toString + ", retrying " + 
              (if (delayMs == 0) "immediately " else "in " + delayMs + " ms"))
          lastException = e
        }
      }
      Thread.sleep(delayMs)
    })
    throw lastException
  }
}
