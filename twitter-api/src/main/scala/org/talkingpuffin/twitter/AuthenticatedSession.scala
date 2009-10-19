package org.talkingpuffin.twitter

import java.net.URL
import scala.xml.{NodeSeq, Node}

/**
* Provides access to Twitter API methods that require authentication.
* Like UnauthenticatedSession, this class is thread safe, and more or less directly mirrors the
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
*/
class AuthenticatedSession(val user: String, val password: String, val apiURL: String) extends UnauthenticatedSession(apiURL){

  private val http = new Http(Some(user), Some(password))

  def this(user: String,password: String) = this(user,password,API.defaultURL)

  override def httpPublisher = http
  
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
        ".xml" + args),http,TwitterStatus.apply).parseXMLList("status").list
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
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/user_timeline/" + urlEncode(id) + ".xml" + args),
        http,TwitterStatus.apply).parseXMLList("status").list
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
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/home_timeline/" + urlEncode(id) + ".xml" + args),
        http,TwitterStatus.apply).parseXMLList("status").list
  }
  def getHomeTimeline(args: TwitterArgs): List[TwitterStatus] = {
    getHomeTimeline(user,args)
  }

  /**
  * @param id the user id <i>or</i> user name who was mentioned
  */
  def getMentions(id: String): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/mentions/" + urlEncode(id) + ".xml"),http,
        TwitterStatus.apply).parseXMLList("status").list
  }
  def getMentions(id: String, page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/mentions/" + urlEncode(id) + ".xml?page=" + page),
        http,TwitterStatus.apply).parseXMLList("status").list
  }

  def getRetweetedByMe(): List[TwitterStatus] = {
    getRetweetedByMe(TwitterArgs())
  }
  def getRetweetedByMe(page: Int): List[TwitterStatus] = {
    getRetweetedByMe(TwitterArgs.page(page))
  }

  def getRetweetedByMe(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/retweeted_by_me.xml" + args),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  def getRetweetedToMe(): List[TwitterStatus] = {
    getRetweetedToMe(TwitterArgs())
  }
  def getRetweetedToMe(page: Int): List[TwitterStatus] = {
    getRetweetedToMe(TwitterArgs.page(page))
  }

  def getRetweetedToMe(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/retweeted_to_me.xml" + args),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  def getRetweetsOfMe(): List[TwitterStatus] = {
    getRetweetsOfMe(TwitterArgs())
  }
  def getRetweetsOfMe(page: Int): List[TwitterStatus] = {
    getRetweetsOfMe(TwitterArgs.page(page))
  }

  def getRetweetsOfMe(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/retweets_of_me.xml" + args),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  /**
  * @param id the user id <i>or</i> user name to get details for
  */
  def getUserDetail(id: String): TwitterUser = {
    new Parser[TwitterUser](new URL(apiURL + "/users/show/" + urlEncode(id) + ".xml"),http,
        TwitterUser.apply).parseXMLElement()
  }

  def getUserDetail(): TwitterUser = {
    getUserDetail(user)
  }
  
  def getReplies(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/replies.xml"),http,
        TwitterStatus.apply).parseXMLList("status").list
  }
  
  def getReplies(page: Int): List[TwitterStatus] = {
    getReplies(TwitterArgs.page(page))
  }

  def getReplies(args: TwitterArgs): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/statuses/replies.xml" + args),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  def getFriends(): XmlResult[TwitterUser] = getFriends(TwitterArgs())
  def getFriends(cursor: Long): XmlResult[TwitterUser] = getFriends(TwitterArgs.cursor(cursor))
  def getFriends(args: TwitterArgs): XmlResult[TwitterUser] =  
      parse("/statuses/friends.xml" + args, TwitterUser.apply, "users", "user")
  
  def getFollowers(): XmlResult[TwitterUser] = getFollowers(TwitterArgs())
  def getFollowers(cursor: Long): XmlResult[TwitterUser] = getFollowers(TwitterArgs.cursor(cursor))
  def getFollowers(args: TwitterArgs): XmlResult[TwitterUser] = 
    parse("/statuses/followers.xml" + args, TwitterUser.apply, "users", "user")
  
  def getFriendsIds(): XmlResult[TwitterUserId] = getFriendsIds(TwitterArgs())
  def getFriendsIds(cursor: Long): XmlResult[TwitterUserId] = getFriendsIds(TwitterArgs().cursor(cursor))
  def getFriendsIds(args: TwitterArgs): XmlResult[TwitterUserId] = 
      parse("/friends/ids.xml" + args, TwitterUserId.apply, "ids", "id")
  
  def getFollowersIds(): XmlResult[TwitterUserId] = getFollowersIds(TwitterArgs())
  def getFollowersIds(cursor: Long): XmlResult[TwitterUserId] = getFollowersIds(TwitterArgs().cursor(cursor))
  def getFollowersIds(args: TwitterArgs): XmlResult[TwitterUserId] = 
      parse("/followers/ids.xml" + args, TwitterUserId.apply, "ids", "id")
  
  def getDirectMessages(): List[TwitterMessage] = {
    getDirectMessages(TwitterArgs())
  }

  def getDirectMessages(page: Int): List[TwitterMessage] = {
    getDirectMessages(TwitterArgs.page(page))
  }

  def getDirectMessages(args: TwitterArgs): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL(apiURL + "/direct_messages.xml" + args),http,
        TwitterMessage.apply).parseXMLList("direct_message").list
  }

  def getSentMessages(): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL(apiURL + "/direct_messages/sent.xml"),http,
        TwitterMessage.apply).parseXMLList("direct_message").list
  }
  
  def getSentMessages(page: Int): List[TwitterMessage] = {
    getSentMessages(TwitterArgs.page(page))
  }

  def createList(listName: String): NodeSeq = {
    http.post(url(user, "lists.xml"), List(("name", listName)))
  }
  
  def getLists(screenName: String): NodeSeq = {
    http.get(url(screenName, "lists.xml"))  // Leave this as XML until it solidifies
  }
  
  def getListNamed(listName: String): Option[NodeSeq] = {
    (getLists(user) \ "list").find(list => (list \ "name").text == listName)
  }
  
  def getListMembers(list: NodeSeq): List[TwitterUser] = {
    (http.get(new URL(apiURL + "/" + (list \ "user" \ "screen_name").text + "/" + (list \ "slug").text + 
        "/members.xml")) \ "users" \ "user").map(TwitterUser.apply).toList
  }
  
  private def url(parts: String*) = new URL((List(apiURL) ::: parts.toList).mkString("/"))
  
  def addToList(listName: String, memberIds: List[Long]): Unit = {
    val slug = ((getListNamed(listName) match {
      case Some(list) => list
      case None => createList(listName)
    }) \ "slug").text
    memberIds.foreach(memberId => {
      http.post(url(user, slug, "members.xml"), List(("id", memberId.toString)))
    })
  }
  
  def getSentMessages(args: TwitterArgs): List[TwitterMessage] = {
    new Parser[TwitterMessage](new URL(apiURL + "/direct_messages/sent.xml" + args),http,
        TwitterMessage.apply).parseXMLList("direct_message").list
  }

  def getFriendshipExists(id1: String, id2: String): Boolean = {
    val xml = http.get(new URL(apiURL + "/friendships/exists.xml?user_a=" + urlEncode(id1) + "&user_b=" + urlEncode(id2)))
    xml match {
      case <friends>true</friends> => true
      case _ => false
    }
  }
  
  def verifyCredentials(): Boolean = {
    try{
      http.get(new URL(apiURL + "/account/verify_credentials.xml"))
      return true
    } catch {
      case e:TwitterNotAuthorized => false
      case e => throw e
    }
  }
  
  def getArchive(): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/account/archive.xml"),http,
        TwitterStatus.apply).parseXMLList("status").list
  }

  def getArchive(page: Int): List[TwitterStatus] = {
    new Parser[TwitterStatus](new URL(apiURL + "/account/archive.xml?page=" + page),http,
        TwitterStatus.apply).parseXMLList("status").list
  }
  
  def updateStatus(status: String): TwitterStatus = {
    val resp = http.post(new URL(apiURL + "/statuses/update.xml?source=talkingpuffin"),List(("status",status)))
    TwitterStatus(resp)
  }

  def updateStatus(status: String, statusId: Long): TwitterStatus = {
    val resp = http.post(new URL(apiURL + "/statuses/update.xml?source=talkingpuffin"),List(("status",status),("in_reply_to_status_id",statusId.toString())))
    TwitterStatus(resp)
  }
  def destroyStatus(statusId: Long): TwitterStatus = {
    val resp = http.delete(new URL(apiURL + "/statuses/destroy/" + statusId.toString() + ".xml"))
    TwitterStatus(resp)
  }
  
  /**
  * @param recipient the user id <i>or</i> user name to send the message to
  * @param text the body of the message
  */
  def newDirectMessage(recipient: String, text: String): TwitterMessage = {
    val resp = http.post(new URL(apiURL + "/direct_messages/new.xml"),List(("user",recipient),("text",text)))
    TwitterMessage(resp)
  }
  
  def destroyDirectMessage(messageId: Long): TwitterMessage = {
    val resp = http.delete(new URL(apiURL + "/direct_messages/destroy/" + messageId.toString() + ".xml"))
    TwitterMessage(resp)
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to create a friendship to
  */
  def createFriendship(friendId: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/friendships/create/" + friendId + ".xml"),Nil)
    TwitterUser(resp)
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to destroy a friendship with
  */
  def destroyFriendship(friendId: String): TwitterUser = {
    val resp = http.delete(new URL(apiURL + "/friendships/destroy/" + friendId + ".xml"))
    TwitterUser(resp)
  }
  
  def updateLocation(location: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/account/update_location.xml"),List(("location",location)))
    TwitterUser(resp)
  }

  def updateDeliveryService(device: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/account/update_delivery_device.xml"),List(("device",device)))
    TwitterUser(resp)
  }
  
  def createFavorite(statusId: Long): TwitterStatus = {
    val resp = http.post(new URL(apiURL + "/favorites/create/" + statusId.toString() + ".xml"),Nil)
    TwitterStatus(resp)
  }

  def destroyFavorite(statusId: Long): TwitterStatus = {
    val resp = http.delete(new URL(apiURL + "/favorites/destroy/" + statusId.toString() + ".xml"))
    TwitterStatus(resp)
  }
  
  def followNotifications(userId: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/notifications/follow/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def leaveNotifications(userId: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/notifications/leave/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def blockUser(userId: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/blocks/create/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def unblockUser(userId: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/blocks/destroy/" + userId.toString() + ".xml"),Nil)
    TwitterUser(resp)
  }

  def reportSpam(userId: String): TwitterUser = {
    val resp = http.post(new URL(apiURL + "/report_spam.xml"), List(("screen_name", userId.toString())))
    TwitterUser(resp)
  }

  @Deprecated def getUserRateLimitStatus(): TwitterRateLimitStatus = {
    new Parser[TwitterRateLimitStatus](new URL(apiURL + "/account/rate_limit_status.xml"),http,
        TwitterRateLimitStatus.apply).parseXMLElement()
  }

  def getFriendIds(id:String) = {
    new Parser[Long](new URL(apiURL + "/friends/ids/" + id + ".xml"),http,
        (node:Node) => node.text.toLong).parseXMLList("id").list
  }

  def getFollowerIds(id:String) = {
    new Parser[Long](new URL(apiURL + "/followers/ids/" + id + ".xml"),http,
        (node:Node) => node.text.toLong).parseXMLList("id").list
  }

  def retweet(id:Long) = {
    val resp = http.post(new URL(apiURL + "/statuses/retweet/" + id + ".xml?source=talkingpuffin"),Nil)
    TwitterStatus(resp)
  }

  override protected def getHttp = http
}


