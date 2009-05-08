package org.talkingpuffin.twitter

import scala.xml._

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
* @author mmcbride
*/
case class TwitterException(twitterMessage: String) extends Throwable
case class TwitterNotModified(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterBadRequest(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterNotAuthorized(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterForbidden(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterNotFound(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterInternalServer(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterBadGateway(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterServiceUnavailable(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterUnknown(override val twitterMessage: String) extends TwitterException(twitterMessage)
case class TwitterBadXML() extends TwitterException("couldn't process XML as requested")

/**
* Factory object to construct TwitterException instances from an HTTP response code
* @author mmcbride
*/
object TwitterException {
  def apply(message: String, code: Int) = code match {
    case 304 => new TwitterNotModified(message)
    case 400 => new TwitterBadRequest(message)
    case 401 => new TwitterNotAuthorized(message)
    case 403 => new TwitterForbidden(message)
    case 404 => new TwitterNotFound(message)
    case 500 => new TwitterInternalServer(message)
    case 502 => new TwitterBadGateway(message)
    case 503 => new TwitterServiceUnavailable(message)
    case _ => new TwitterUnknown(message)
  }
}
