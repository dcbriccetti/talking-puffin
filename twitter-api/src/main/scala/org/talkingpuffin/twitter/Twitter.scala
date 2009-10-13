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
  def apply(user: String, password: String): AuthenticatedSession = {
    new AuthenticatedSession(user,password)
  }

  def apply(user: String, password: String, apiURL: String): AuthenticatedSession = {
    new AuthenticatedSession(user,password,apiURL)
  }
  
  /**
  * get an UnauthenticatedSession instance
  */
  def apply(): UnauthenticatedSession = {
    new UnauthenticatedSession()
  }

  def apply(apiURL: String): UnauthenticatedSession = {
    new UnauthenticatedSession(apiURL)
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
class UnauthenticatedSession(apiURL: String) extends TwitterSession{

  def this() = this(API.defaultURL)
  
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
  private val http = new Http(null,null)
  
  def getPublicTimeline(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/public_timeline.xml"),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getPublicTimeline(page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/public_timeline.xml?page=" + page),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getStatus(id: Long): TwitterStatus = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/show/" + id.toString() + ".xml"),http,TwitterStatus.apply).parseXMLElement()
  }
  
  def getFeatured(): List[TwitterUser] = {
    new Parser[TwitterUser](new URL(apiURL + "/statuses/featured.xml"),http,TwitterUser.apply).parseXMLList("user")
  }

  /**
  * @param id the user id <i>or</i> user name to get favorites for
  */
  def getFavorites(id: String): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/favorites/" + urlEncode(id) + ".xml"),http,TwitterStatus.apply).parseXMLList("status")
  }
  
  /**
  * @param id the user id <i>or</i> user name to get favorites for
  * @param page the results page to fetch.
  */
  def getFavorites(id: String, page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/favorites/" + urlEncode(id) + ".xml?page=" + page.toString()),http,TwitterStatus.apply).parseXMLList("status")
  }

  /**
  * @param id the user id <i>or</i> user name to get friends for
  */
  def getFriends(id: String): List[TwitterUser] = {
    getFriends(id,TwitterArgs())
  }

  /**
  * @param id the user id <i>or</i> user name to get friends for
  * @param page the results page to fetch.
  */
  def getFriends(id: String, page: Int): List[TwitterUser] = {
    getFriends(id,TwitterArgs().page(page))
  }

  def getFriends(id: String, args: TwitterArgs): List[TwitterUser] = {
    new Parser[TwitterUser](new URL(apiURL + "/statuses/friends/" + urlEncode(id) + ".xml" + args),http,TwitterUser.apply).parseXMLList("user")
  }
  
  protected def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")
}

/**
* Provides access to Twitter API methods that require authentication.
* Like UnauthenticatedSession, this class is thread safe, and more or less directly mirrors the
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
*/
class AuthenticatedSession(val user: String, val password: String, val apiURL: String) extends UnauthenticatedSession(apiURL){

  private val http = new Http(user,password)

  def this(user: String,password: String) = this(user,password,API.defaultURL)
  /**
  * @param id the user id <i>or</i> user name of the desired friends timeline
  */
  def getFriendsTimeline(id: String): List[TwitterStatus] = {
    getFriendsTimeline(id,TwitterArgs())
  }

  def getFriendsTimeline(id: String,page: Int): List[TwitterStatus] = {
    getFriendsTimeline(id,TwitterArgs.page(page))
  }

  def getFriendsTimeline(id: String, args:TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/friends_timeline/" + urlEncode(id) + 
        ".xml" + args),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getFriendsTimeline(): List[TwitterStatus] = {
    getFriendsTimeline(user,TwitterArgs())
  }

  def getFriendsTimeline(page: Int): List[TwitterStatus] = {
    getFriendsTimeline(user,TwitterArgs.page(page))
  }

  def getFriendsTimeline(args:TwitterArgs): List[TwitterStatus] = {
    getFriendsTimeline(user,args)
  }

  /**
  * @param id the user id <i>or</i> user name of the desired user's timeline
  */
  def getUserTimeline(id: String): List[TwitterStatus] = {
    getUserTimeline(id,TwitterArgs())
  }
  def getUserTimeline(id: String, page: Int): List[TwitterStatus] = {
    getUserTimeline(id,TwitterArgs.page(page))
  }

  def getUserTimeline(id: String, args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/user_timeline/" + urlEncode(id) + ".xml" + args),http,TwitterStatus.apply).parseXMLList("status")
  }

  /**
  * @param id the user id <i>or</i> user name of the desired user's timeline with retweets
  */
  def getHomeTimeline(id: String): List[TwitterStatus] = {
    getHomeTimeline(id,TwitterArgs())
  }
  def getHomeTimeline(id: String, page: Int): List[TwitterStatus] = {
    getHomeTimeline(id,TwitterArgs.page(page))
  }

  def getHomeTimeline(id: String, args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/home_timeline/" + urlEncode(id) + ".xml" + args),http,TwitterStatus.apply).parseXMLList("status")
  }
  def getHomeTimeline(args: TwitterArgs): List[TwitterStatus] = {
    getHomeTimeline(user,args)
  }

  /**
  * @param id the user id <i>or</i> user name who was mentioned
  */
  def getMentions(id: String): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/mentions/" + urlEncode(id) + ".xml"),http,TwitterStatus.apply).parseXMLList("status")
  }
  def getMentions(id: String, page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/mentions/" + urlEncode(id) + ".xml?page=" + page),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getRetweetedByMe(): List[TwitterStatus] = {
    getRetweetedByMe(TwitterArgs())
  }
  def getRetweetedByMe(page: Int): List[TwitterStatus] = {
    getRetweetedByMe(TwitterArgs.page(page))
  }

  def getRetweetedByMe(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/retweeted_by_me.xml" + args),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getRetweetedToMe(): List[TwitterStatus] = {
    getRetweetedToMe(TwitterArgs())
  }
  def getRetweetedToMe(page: Int): List[TwitterStatus] = {
    getRetweetedToMe(TwitterArgs.page(page))
  }

  def getRetweetedToMe(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/retweeted_to_me.xml" + args),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getRetweetsOfMe(): List[TwitterStatus] = {
    getRetweetsOfMe(TwitterArgs())
  }
  def getRetweetsOfMe(page: Int): List[TwitterStatus] = {
    getRetweetsOfMe(TwitterArgs.page(page))
  }

  def getRetweetsOfMe(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/retweets_of_me.xml" + args),http,TwitterStatus.apply).parseXMLList("status")
  }

  /**
  * @param id the user id <i>or</i> user name to get details for
  */
  def getUserDetail(id: String): TwitterUser = {
    new Parser[TwitterUser](new URL(apiURL + "/users/show/" + urlEncode(id) + ".xml"),http,TwitterUser.apply).parseXMLElement()
  }

  def getUserDetail(): TwitterUser = {
    getUserDetail(user)
  }
  
  def getReplies(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/replies.xml"),http,TwitterStatus.apply).parseXMLList("status")
  }
  
  def getReplies(page: Int): List[TwitterStatus] = {
    getReplies(TwitterArgs.page(page))
  }

  def getReplies(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/replies.xml" + args),http,TwitterStatus.apply).parseXMLList("status")
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
  
  def getFollowers(): List[TwitterUser] = {
    getFollowers(TwitterArgs())
  }

  def getFollowers(page: Int): List[TwitterUser] = {
    getFollowers(TwitterArgs().page(page))
  }

  def getFollowers(args: TwitterArgs): List[TwitterUser] = {
    new Parser[TwitterUser](new URL(apiURL + "/statuses/followers.xml" + args),http,TwitterUser.apply).parseXMLList("user")
  }
  
  def getFriendsIds(): List[TwitterUserId] = getFriendsIds(TwitterArgs())

  def getFriendsIds(page: Int): List[TwitterUserId] = getFriendsIds(TwitterArgs().page(page))

  def getFriendsIds(args: TwitterArgs): List[TwitterUserId] = 
    new Parser[TwitterUserId](new URL(apiURL + "/friends/ids.xml" + args),http,TwitterUserId.apply).parseXMLList("id")
  
  def getFollowersIds(): List[TwitterUserId] = getFollowersIds(TwitterArgs())

  def getFollowersIds(page: Int): List[TwitterUserId] = getFollowersIds(TwitterArgs().page(page))

  def getFollowersIds(args: TwitterArgs): List[TwitterUserId] = 
    new Parser[TwitterUserId](new URL(apiURL + "/followers/ids.xml" + args),http,TwitterUserId.apply).parseXMLList("id")
  
  def getDirectMessages(): List[TwitterMessage] = {
    getDirectMessages(TwitterArgs())
  }

  def getDirectMessages(page: Int): List[TwitterMessage] = {
    getDirectMessages(TwitterArgs.page(page))
  }

  def getDirectMessages(args: TwitterArgs): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL(apiURL + "/direct_messages.xml" + args),http,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getSentMessages(): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL(apiURL + "/direct_messages/sent.xml"),http,TwitterMessage.apply).parseXMLList("direct_message")
  }
  
  def getSentMessages(page: Int): List[TwitterMessage] = {
    getSentMessages(TwitterArgs.page(page))
  }

  def getSentMessages(args: TwitterArgs): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL(apiURL + "/direct_messages/sent.xml" + args),http,TwitterMessage.apply).parseXMLList("direct_message")
  }

  def getFriendshipExists(id1: String, id2: String): Boolean = {
    val xml = http.doGet(new URL(apiURL + "/friendships/exists.xml?user_a=" + urlEncode(id1) + "&user_b=" + urlEncode(id2)))
    xml match {
      case <friends>true</friends> => true
      case _ => false
    }
  }
  
  def verifyCredentials(): Boolean = {
    try{
      http.doGet(new URL(apiURL + "/account/verify_credentials.xml"))
      return true
    } catch {
      case e:TwitterNotAuthorized => false
      case e => throw e
    }
  }
  
  def getArchive(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/account/archive.xml"),http,TwitterStatus.apply).parseXMLList("status")
  }

  def getArchive(page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/account/archive.xml?page=" + page),http,TwitterStatus.apply).parseXMLList("status")
  }
  
  def updateStatus(status: String): TwitterStatus = {
    val resp = http.doPost(new URL(apiURL + "/statuses/update.xml?source=talkingpuffin"),List(("status",status)))
    TwitterStatus(resp)
  }

  def updateStatus(status: String, statusId: Long): TwitterStatus = {
    val resp = http.doPost(new URL(apiURL + "/statuses/update.xml?source=talkingpuffin"),List(("status",status),("in_reply_to_status_id",statusId.toString())))
    TwitterStatus(resp)
  }
  def destroyStatus(statusId: Long): TwitterStatus = {
    val resp = http.doDelete(new URL(apiURL + "/statuses/destroy/" + statusId.toString() + ".xml"))
    TwitterStatus(resp)
  }
  
  /**
  * @param recipient the user id <i>or</i> user name to send the message to
  * @param text the body of the message
  */
  def newDirectMessage(recipient: String, text: String): TwitterMessage = {
    val resp = http.doPost(new URL(apiURL + "/direct_messages/new.xml"),List(("user",recipient),("text",text)))
    TwitterMessage(resp)
  }
  
  def destroyDirectMessage(messageId: Long): TwitterMessage = {
    val resp = http.doDelete(new URL(apiURL + "/direct_messages/destroy/" + messageId.toString() + ".xml"))
    TwitterMessage(resp)
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to create a friendship to
  */
  def createFriendship(friendId: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/friendships/create/" + friendId + ".xml"),Nil)
    TwitterUser(resp)
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to destroy a friendship with
  */
  def destroyFriendship(friendId: String): TwitterUser = {
    val resp = http.doDelete(new URL(apiURL + "/friendships/destroy/" + friendId + ".xml"))
    TwitterUser(resp)
  }
  
  def updateLocation(location: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/account/update_location.xml"),List(("location",location)))
    TwitterUser(resp)
  }

  def updateDeliveryService(device: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/account/update_delivery_device.xml"),List(("device",device)))
    TwitterUser(resp)
  }
  
  def createFavorite(statusId: Long): TwitterStatus = {
    val resp = http.doPost(new URL(apiURL + "/favorites/create/" + statusId.toString() + ".xml"),Nil)
    TwitterStatus(resp)
  }

  def destroyFavorite(statusId: Long): TwitterStatus = {
    val resp = http.doDelete(new URL(apiURL + "/favorites/destroy/" + statusId.toString() + ".xml"))
    TwitterStatus(resp)
  }
  
  def followNotifications(userId: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/notifications/follow/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def leaveNotifications(userId: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/notifications/leave/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def blockUser(userId: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/blocks/create/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def unblockUser(userId: String): TwitterUser = {
    val resp = http.doPost(new URL(apiURL + "/blocks/destroy/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def getUserRateLimitStatus(): TwitterRateLimitStatus = {
    new Parser[TwitterRateLimitStatus](new URL(apiURL + "/account/rate_limit_status.xml"),http,TwitterRateLimitStatus.apply).parseXMLElement()
  }

  def getFriendIds(id:String) = {
    new Parser[Long](new URL(apiURL + "/friends/ids/" + id + ".xml"),http,(node:Node) => node.text.toLong).parseXMLList("id")
  }

  def getFollowerIds(id:String) = {
    new Parser[Long](new URL(apiURL + "/followers/ids/" + id + ".xml"),http,(node:Node) => node.text.toLong).parseXMLList("id")
  }

  def retweet(id:Long) = {
    val resp = http.doPost(new URL(apiURL + "/statuses/retweet/" + id + ".xml?source=talkingpuffin"),Nil)
    TwitterStatus(resp)
  }
}

// end session apiURL + /account/end_session
// help apiURL + /help/test.format
// downtime apiURL + /help/downtime_schedule.format

