package org.talkingpuffin.twitter

import org.apache.log4j.Logger

/**
* Handles an arbitrary IOException while connecting to Twitter.
* Possible response codes from the Twitter doc...
* <ul>
* <li>200 OK: everything went awesome.</li>
* <li>304 Not Modified: there was no new data to return.</li>
* <li>400 Bad Request: your request is invalid, and we'll return an error message that tells you why. This is the status code returned if you've exceeded the rate limit. </li>
* <li>401 Not Authorized: either you need to provide authentication credentials, or the credentials provided aren't valid.</li>
* <li>403 Forbidden: we understand your request, but are refusing to fulfill it.  An accompanying error message should explain why.</li>
* <li>404 Not Found: either you're requesting an invalid URI or the resource in question doesn't exist (ex: no such user). </li>
* <li>500 Internal Server Error: we did something wrong.  Please post to the group about it and the Twitter team will investigate.</li>
* <li>502 Bad Gateway: returned if Twitter is down or being upgraded.</li>
* <li>503 Service Unavailable: the Twitter servers are up, but are overloaded with requests.  Try again later.</li>
* </ul>
*/
case class TwitterException(val code: Int, twitterMessage: String) extends Throwable
case class TwitterNotModified(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterBadRequest(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterNotAuthorized(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterForbidden(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterNotFound(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterInternalServer(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterBadGateway(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterServiceUnavailable(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)
case class TwitterUnknown(override val code: Int, override val twitterMessage: String) extends TwitterException(code, twitterMessage)

/**
* Factory object to construct TwitterException instances from an HTTP response code
*/
object TwitterException extends Exception {
  private val log = Logger.getLogger("TwitterException")
  private val titleRegEx = """.*?<title>(.*?)</title>.*""".r
  
  def apply(message: String, code: Int) = {
    val title = message match { // Error message may appear in title of an HTML page
      case titleRegEx(t) => Some(t)
      case _ => None
    }
    log.debug("Code: " + code + 
        (title match {case Some(t) => ", Title: " + t case None => ", Message: " + message}))
    
    code match {
      case 304 => new TwitterNotModified(code, message)
      case 400 => new TwitterBadRequest(code, message)
      case 401 => new TwitterNotAuthorized(code, message)
      case 403 => new TwitterForbidden(code, message)
      case 404 => new TwitterNotFound(code, message)
      case 500 => new TwitterInternalServer(code, message)
      case 502 => new TwitterBadGateway(code, message)
      case 503 => new TwitterServiceUnavailable(code, message)
      case _ => new TwitterUnknown(code, message)
    }
  }
}
