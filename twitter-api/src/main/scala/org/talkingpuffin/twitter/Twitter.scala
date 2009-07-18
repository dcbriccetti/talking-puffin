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
*/
class UnauthenticatedSession() extends TwitterSession{

  def loadAll[T](f:(Int) => List[T]):List[T] = {
    loadAll(1,f,List[T]())
  }

  private def loadAll[T](page: Int, f:(Int) => List[T], listIn: List[T]):List[T] = {
      f(page) match {
          case Nil => listIn
          case l => loadAll(page+1,f,listIn ::: l)
      }
  }
  /** utility class to connect to a URL and fetch XML. */
  val fetcher = new XMLFetcher(null,null)
  
  def getPublicTimeline() :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/public_timeline.xml"),fetcher,TwitterStatus.apply).parseXMLList("status")
  }

  def getPublicTimeline(page: Int) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/public_timeline.xml?page=" + page),fetcher,TwitterStatus.apply).parseXMLList("status")
  }

  def getStatus(id: Long) :TwitterStatus = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/show/" + id.toString() + ".xml"),fetcher,TwitterStatus.apply).parseXMLElement()
  }
  
  def getFeatured() :List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/featured.xml"),fetcher,TwitterUser.apply).parseXMLList("user")
  }

  /**
  * @param id the user id <i>or</i> user name to get favorites for
  */
  def getFavorites(id: String) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/favorites/" + urlEncode(id) + ".xml"),fetcher,TwitterStatus.apply).parseXMLList("status")
  }
  
  /**
  * @param id the user id <i>or</i> user name to get favorites for
  * @param page the results page to fetch.
  */
  def getFavorites(id: String, page: Int) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/favorites/" + urlEncode(id) + ".xml?page=" + page.toString()),fetcher,TwitterStatus.apply).parseXMLList("status")
  }

  /**
  * @param id the user id <i>or</i> user name to get friends for
  */
  def getFriends(id: String) :List[TwitterUser] = {
    getFriends(id,TwitterArgs())
  }

  /**
  * @param id the user id <i>or</i> user name to get friends for
  * @param page the results page to fetch.
  */
  def getFriends(id: String, page: Int) :List[TwitterUser] = {
    getFriends(id,TwitterArgs().page(page))
  }

  def getFriends(id: String, args: TwitterArgs): List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/friends/" + urlEncode(id) + ".xml" + args),fetcher,TwitterUser.apply).parseXMLList("user")
  }
  
  protected def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")
}

/**
* Provides access to Twitter API methods that require authentication.
* Like UnauthenticatedSession, this class is thread safe, and more or less directly mirrors the
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
*/
class AuthenticatedSession(val user: String, password: String) extends UnauthenticatedSession{

  val authFetcher = new XMLFetcher(user,password)
  
  /**
  * @param id the user id <i>or</i> user name of the desired friends timeline
  */
  def getFriendsTimeline(id: String) :List[TwitterStatus] = {
    getFriendsTimeline(id,TwitterArgs())
  }

  def getFriendsTimeline(id: String,page: Int) :List[TwitterStatus] = {
    getFriendsTimeline(id,TwitterArgs.page(page))
  }

  def getFriendsTimeline(id: String, args:TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/friends_timeline/" + urlEncode(id) + ".xml" + args),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }

  def getFriendsTimeline() :List[TwitterStatus] = {
    getFriendsTimeline(user,TwitterArgs())
  }

  def getFriendsTimeline(page: Int) :List[TwitterStatus] = {
    getFriendsTimeline(user,TwitterArgs.page(page))
  }

  def getFriendsTimeline(args:TwitterArgs): List[TwitterStatus] = {
    getFriendsTimeline(user,args)
  }

  /**
  * @param id the user id <i>or</i> user name of the desired user's timeline
  */
  def getUserTimeline(id: String) :List[TwitterStatus] = {
    getUserTimeline(id,TwitterArgs())
  }
  def getUserTimeline(id: String, page: Int) :List[TwitterStatus] = {
    getUserTimeline(id,TwitterArgs.page(page))
  }

  def getUserTimeline(id: String, args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/user_timeline/" + urlEncode(id) + ".xml" + args),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  /**
  * @param id the user id <i>or</i> user name who was mentioned
  */
  def getMentions(id: String) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/mentions/" + urlEncode(id) + ".xml"),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  def getMentions(id: String, page: Int) :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/mentions/" + urlEncode(id) + ".xml?page=" + page),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  /**
  * @param id the user id <i>or</i> user name to get details for
  */
  def getUserDetail(id: String) :TwitterUser = {
    new Parser[TwitterUser](new URL("http://twitter.com/users/show/" + urlEncode(id) + ".xml"),authFetcher,TwitterUser.apply).parseXMLElement()
  }

  def getUserDetail(): TwitterUser = {
    getUserDetail(user)
  }
  
  def getReplies() :List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/replies.xml"),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  
  def getReplies(page: Int) :List[TwitterStatus] = {
    getReplies(TwitterArgs.page(page))
  }

  def getReplies(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/statuses/replies.xml" + args),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }

  def getFriends(): List[TwitterUser] = {
    getFriends(user)
  }

  def getFriends(page: Int): List[TwitterUser] = {
    getFriends(user,page)
  }

  def getFriends(args: TwitterArgs):List[TwitterUser] = {
    getFriends(user,args)
  }
  
  def getFollowers() :List[TwitterUser] = {
    getFollowers(TwitterArgs())
  }

  def getFollowers(page: Int) :List[TwitterUser] = {
    getFollowers(TwitterArgs().page(page))
  }

  def getFollowers(args: TwitterArgs): List[TwitterUser] = {
    new Parser[TwitterUser](new URL("http://twitter.com/statuses/followers.xml" + args),authFetcher,TwitterUser.apply).parseXMLList("user")
  }
  
  def getDirectMessages() :List[TwitterMessage] = {
    getDirectMessages(TwitterArgs())
  }

  def getDirectMessages(page :Int) :List[TwitterMessage] = {
    getDirectMessages(TwitterArgs.page(page))
  }

  def getDirectMessages(args: TwitterArgs): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages.xml" + args),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getSentMessages() :List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages/sent.xml"),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }
  
  def getSentMessages(page: Int) :List[TwitterMessage] = {
    getSentMessages(TwitterArgs.page(page))
  }

  def getSentMessages(args: TwitterArgs): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL("http://twitter.com/direct_messages/sent.xml" + args),authFetcher,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getFriendshipExists(id1: String, id2: String): Boolean = {
    val xml = authFetcher.doGet(new URL("http://twitter.com/friendships/exists.xml?user_a=" + urlEncode(id1) + "&user_b=" + urlEncode(id2)))
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

  def getArchive(page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL("http://twitter.com/account/archive.xml?page=" + page),authFetcher,TwitterStatus.apply).parseXMLList("status")
  }
  
  def updateStatus(status: String) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/statuses/update.xml?source=talkingpuffin"),List(("status",status)))
    TwitterStatus(resp)
  }

  def updateStatus(status: String, statusId: Long) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/statuses/update.xml?source=talkingpuffin"),List(("status",status),("in_reply_to_status_id",statusId.toString())))
    TwitterStatus(resp)
  }
  def destroyStatus(statusId: Long) :TwitterStatus = {
    val resp = authFetcher.doDelete(new URL("http://twitter.com/statuses/destroy/" + statusId.toString() + ".xml"))
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
  
  def destroyDirectMessage(messageId: Long) :TwitterMessage = {
    val resp = authFetcher.doDelete(new URL("http://twitter.com/direct_messages/destroy/" + messageId.toString() + ".xml"))
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
    val resp = authFetcher.doDelete(new URL("http://twitter.com/friendships/destroy/" + friendId + ".xml"))
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
  
  def createFavorite(statusId: Long) :TwitterStatus = {
    val resp = authFetcher.doPost(new URL("http://twitter.com/favorites/create/" + statusId.toString() + ".xml"),Nil)
    TwitterStatus(resp)
  }

  def destroyFavorite(statusId: Long) :TwitterStatus = {
    val resp = authFetcher.doDelete(new URL("http://twitter.com/favorites/destroy/" + statusId.toString() + ".xml"))
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

  def getUserRateLimitStatus(): TwitterRateLimitStatus = {
    new Parser[TwitterRateLimitStatus](new URL("http://twitter.com/account/rate_limit_status.xml"),authFetcher,TwitterRateLimitStatus.apply).parseXMLElement()
  }
}

// end session http://twitter.com/account/end_session
// help http://twitter.com/help/test.format 
// downtime http://twitter.com/help/downtime_schedule.format

