package org.talkingpuffin.twitter

import java.net.{URL,URLEncoder}
import scala.xml._

/**
* The TwitterSession object acts as a factory for both authenticated and unauthenticated sessions.
* There are two apply methods.
* <tt><pre>
* val session = TwitterSession()
* </pre></tt>
* will return an UnauthenticatedSession instance, which only supports methods which do not require logon.
* <tt><pre>
* val session = TwitterSession(userid,password)
* </pre></tt>
* will return an AuthenticatedSession.  This extends UnauthenticatedSession, and provides additional
* calls that require authentication
* @author mmcbride
*/
object TwitterSession {
  /**
  * get an AuthenticatedSession instance with the provided user and password
  * Note that this should always succeed.  The userid and password are not (currently) checked
  */
  def apply(user: String, password: String) :AuthenticatedSession = {
    new AuthenticatedSession(user,password)
  }
  
  /**
  * get an UnauthenticatedSession instance
  */
  def apply() :UnauthenticatedSession = {
    new UnauthenticatedSession()
  }
}

/**
* The base class of both TwitterSession objects
* @author mmcbride
*/
abstract class TwitterSession

/**
* Provides an interface to Twitter for all non-authorized calls.
* This class provides a limited set of functionality, but should be
* safe to use against the 70 calls/hr rate limit.
*
* If you need access to the authenticated calls, see AuthenticatedSession.
*
* This class should be completely thread safe, allowing multiple simultaneous calls to Twitter via this object.
*
* All methods are fairly direct representations of calls specified in the 
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
* @author mmcbride
*/
class UnauthenticatedSession() extends TwitterSession{
  
  /** utility class to connect to a URL and fetch XML. */
  val fetcher = new XMLFetcher(null,null)
  
  def getPublicTimeline() :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/public_timeline.xml"),fetcher,TwitterStatus.apply).parseXMLList("status")
  }

  def getStatus(id: Int) :TwitterStatus = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/show/" + id.toString() + ".xml"),fetcher,TwitterStatus.apply).parseXMLElement()
  }
  
  def getFeatured() :List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/featured.xml"),fetcher,TwitterUser.apply).parseXMLList("user")
  }

  /**
  * @param id the user id <i>or</i> user name to get favorites for
  */
  def getFavorites(id: String) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/favorites/" + URLEncoder.encode(id) + ".xml"),fetcher,TwitterStatus.apply).parseXMLList("status")
  }
  
  /**
  * @param id the user id <i>or</i> user name to get favorites for
  * @param page the results page to fetch.
  */
  def getFavorites(id: String, page: Int) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/favorites/" + URLEncoder.encode(id) + ".xml?page=" + page.toString()),fetcher,TwitterStatus.apply).parseXMLList("status")
  }

  /**
  * @param id the user id <i>or</i> user name to get friends for
  */
  def getFriends(id: String) :List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/friends/" + URLEncoder.encode(id) + ".xml"),fetcher,TwitterUser.apply).parseXMLList("user")
  }
  /**
  * @param id the user id <i>or</i> user name to get friends for
  * @param page the results page to fetch.
  */
  def getFriends(id: String, page: Int) :List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/friends/" + URLEncoder.encode(id) + ".xml?page=" + page.toString()),fetcher,TwitterUser.apply).parseXMLList("user")
  }
}

/**
* Provides access to Twitter API methods that require authentication.
* Like UnauthenticatedSession, this class is thread safe, and more or less directly mirrors the
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
* @author mmcbride
*/
class AuthenticatedSession(user: String, password: String) extends UnauthenticatedSession{

  val authFetcher = new XMLFetcher(user,password)
  
  /**
  * @param id the user id <i>or</i> user name of the desired friends timeline
  */
  def getFriendsTimeline(id: String) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/friends_timeline/" + URLEncoder.encode(id) + ".xml"),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }

  /**
  * @param id the user id <i>or</i> user name to get details for
  */
  def getUserDetail(id: String) :TwitterUser = {
    new Parser[TwitterUser](new URL("http://twitter.com/users/show/" + URLEncoder.encode(id) + ".xml"),authFetcher,TwitterUser.apply).parseXMLElement()
  }
  
  def getReplies() :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/replies.xml"),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  
  def getReplies(page: Int) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/replies.xml?page=" + page.toString()),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }

  def getFollowers() :List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/followers.xml"),authFetcher,TwitterUser.apply).parseXMLList("user")
  }

  def getFollowers(page: Int) :List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/followers.xml?page=" + page.toString()),authFetcher,TwitterUser.apply).parseXMLList("user")
  }

  def getDirectMessages() :List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages.xml"),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getDirectMessages(page :Int) :List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages.xml?page=" + page.toString()),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getSentMessages() :List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages/sent.xml"),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }
  
  def getSentMessages(page: Int) :List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages/sent.xml?page=" + page.toString()),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getFriendshipExists(id1: String, id2: String): Boolean = {
    val xml = authFetcher.doGet(new URL("http://twitter.com/friendships/exists.xml?user_a=" + URLEncoder.encode(id1) + "&user_b=" + URLEncoder.encode(id2)))
    xml match {
      case <friends>true</friends> => true
      case _ => false
    }
  }
  
  def verifyCredentials(): Boolean = {
    try{
      authFetcher.doGet(new URL("http://twitter.com/account/verify_credentials.xml"))
      return true
    } catch {
      case e:TwitterNotAuthorized => false
      case e => throw e
    }
  }
  
  def getArchive(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/account/archive.xml"),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  
  def updateStatus(status: String) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/statuses/update.xml"),List(("status",status)))
    TwitterStatus(resp)
  }
  def destroyStatus(statusId: Int) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/statuses/destroy/" + statusId.toString() + ".xml"),Nil)
    TwitterStatus(resp)
  }
  
  /**
  * @param recipient the user id <i>or</i> user name to send the message to
  * @param text the body of the message
  */
  def newDirectMessage(recipient: String, text: String) :TwitterMessage = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/direct_messages/new.xml"),List(("user",recipient),("text",text)))
    TwitterMessage(resp)
  }
  
  def destroyDirectMessage(messageId: Int) :TwitterMessage = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/direct_messages/destroy/" + messageId.toString() + ".xml"),Nil)
    TwitterMessage(resp)
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to create a friendship to
  */
  def createFriendship(friendId: String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/friendships/create/" + friendId + ".xml"),Nil)
    TwitterUser(resp)
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to destroy a friendship with
  */
  def destroyFriendship(friendId: String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/friendships/destroy/" + friendId + ".xml"),Nil)
    TwitterUser(resp)
  }
  
  def updateLocation(location :String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/account/update_location.xml"),List(("location",location)))
    TwitterUser(resp)
  }

  def updateDeliveryService(device :String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/account/update_delivery_device.xml"),List(("device",device)))
    TwitterUser(resp)
  }
  
  def createFavorite(statusId: Int) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/favorites/create/" + statusId.toString() + ".xml"),Nil)
    TwitterStatus(resp)
  }

  def destroyFavorite(statusId: Int) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/favorites/destroy/" + statusId.toString() + ".xml"),Nil)
    TwitterStatus(resp)
  }
  
  def followNotifications(userId: String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/notifications/follow/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def leaveNotifications(userId: String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/notifications/leave/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def blockUser(userId: String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/blocks/create/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def unblockUser(userId: String) :TwitterUser = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/blocks/destroy/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }
}

// end session http://twitter.com/account/end_session
// help http://twitter.com/help/test.format 
// downtime http://twitter.com/help/downtime_schedule.format

