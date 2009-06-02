/*
 * TwitterRateLimitStatus.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.talkingpuffin.twitter

import java.util.Date
import scala.xml._
import org.apache.log4j._
import org.joda.time._

/**
* Represents a twitter rate limit status object
*/
class TwitterRateLimitStatus() extends Validated{
  var remainingHits:Int = 0
  var hourlyLimit:Int = 0
  var resetTime:DateTime = null
  var resetTimeInSeconds:Int = 0
  def isValid() = {
    hourlyLimit != 0 &&
    //resetTime != null &&
    resetTimeInSeconds != 0
  }
}

/**
*
* The TwitterStatus object is used to construct TwitterStatus instances from XML fragments
* The only method available is apply, which allows you to use the object as follows
* <tt><pre>
* val xml = getXML()
* val status = TwitterStatus(xml)
* </pre></tt>
* @author mmcbride
*/
object TwitterRateLimitStatus{
  val logger = Logger.getLogger("twitter")

  /**
  * construct a TwitterStatus object from an XML node
  */
  def apply(n: Node):TwitterRateLimitStatus = {
    val status = new TwitterRateLimitStatus
    n.child foreach {(sub) =>
      sub match {
        case <remaining-hits>{Text(text)}</remaining-hits> => status.remainingHits = Integer.parseInt(text)
        case <hourly-limit>{Text(text)}</hourly-limit> => status.hourlyLimit = Integer.parseInt(text)
        case <reset-time-in-seconds>{Text(text)}</reset-time-in-seconds> => status.resetTimeInSeconds = Integer.parseInt(text)
        case <reset-time>{Text(text)}</reset-time> => status.resetTime = new DateTime(text)
        case _ => Nil
      }
    }
    return status
  }
}
