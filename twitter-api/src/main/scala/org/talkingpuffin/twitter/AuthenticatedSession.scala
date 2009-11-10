package org.talkingpuffin.twitter

import scala.xml.Node

/**
* Provides access to Twitter API methods that require authentication.
* Like UnauthenticatedSession, this class is thread safe, and more or less directly mirrors the
* <a href="http://groups.google.com/group/twitter-development-talk/web/api-documentation">Twitter API Doc</a>
*/
class AuthenticatedSession(val user: String, val password: String, val apiURL: String) extends UnauthenticatedSession(apiURL){

  private val http = new Http(Some(user), Some(password)) {suppressLogPrefix = apiURL}

  def this(user: String,password: String) = this(user,password,API.defaultURL)

  override def httpPublisher = http
  
  implicit def page2TwitterArgs(page: Int) = TwitterArgs.page(page) 

  
  /**
  * @param id the user id <i>or</i> user name of the desired friends timeline
  */
  def getFriendsTimeline(id: String): List[TwitterStatus] = getFriendsTimeline(id,TwitterArgs())

  def getFriendsTimeline(id: String, args:TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/friends_timeline/" + urlEncode(id) + ".xml" + args, 
        TwitterStatus.apply, "status").list
  }

  def getFriendsTimeline(): List[TwitterStatus] = getFriendsTimeline(user,TwitterArgs())
  def getFriendsTimeline(args:TwitterArgs): List[TwitterStatus] = getFriendsTimeline(user,args)

  /**
  * @param id the user id <i>or</i> user name of the desired user's timeline
  */
  def getUserTimeline(id: String): List[TwitterStatus] = getUserTimeline(id,TwitterArgs())

  def getUserTimeline(id: String, args: TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/user_timeline/" + urlEncode(id) + ".xml" + args,  
      TwitterStatus.apply, "status").list
  }

  /**
  * @param id the user id <i>or</i> user name of the desired user's timeline with retweets
  */
  def getHomeTimeline(id: String): List[TwitterStatus] = getHomeTimeline(id,TwitterArgs())

  def getHomeTimeline(id: String, args: TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/home_timeline/" + urlEncode(id) + ".xml" + args, TwitterStatus.apply,
        "status").list
  }
  
  def getHomeTimeline(args: TwitterArgs): List[TwitterStatus] = getHomeTimeline(user,args)

  def getFavorites(args: TwitterArgs): List[TwitterStatus] = getFavorites(user,args)
  
  /**
  * @param id the user id <i>or</i> user name who was mentioned
  */
  def getMentions(id: String): List[TwitterStatus] = {
    parse("/statuses/mentions/" + urlEncode(id) + ".xml", TwitterStatus.apply, "status").list
  }
  def getMentions(id: String, page: Int): List[TwitterStatus] = {
    parse("/statuses/mentions/" + urlEncode(id) + ".xml?page=" + page, TwitterStatus.apply, "status").list
  }

  def getRetweetedByMe(): List[TwitterStatus] = getRetweetedByMe(TwitterArgs())

  def getRetweetedByMe(args: TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/retweeted_by_me.xml" + args, TwitterStatus.apply, "status").list
  }

  def getRetweetedToMe(): List[TwitterStatus] = getRetweetedToMe(TwitterArgs())

  def getRetweetedToMe(args: TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/retweeted_to_me.xml" + args, TwitterStatus.apply, "status").list
  }
  
  def getRetweetsOfMe(): List[TwitterStatus] = getRetweetsOfMe(TwitterArgs())

  def getRetweetsOfMe(args: TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/retweets_of_me.xml" + args, TwitterStatus.apply, "status").list
  }

  /**
  * @param id the user id <i>or</i> user name to get details for
  */
  def getUserDetail(id: String): TwitterUser = {
    new Parser[TwitterUser](url("users/show/" + urlEncode(id) + ".xml"), http,
        TwitterUser.apply).parseXMLElement()
  }

  def getUserDetail(): TwitterUser = getUserDetail(user)
  
  def getReplies(): List[TwitterStatus] = {
    parse("/statuses/replies.xml", TwitterStatus.apply, "status").list
  }
  
  def getReplies(args: TwitterArgs): List[TwitterStatus] = {
    parse("/statuses/replies.xml" + args, TwitterStatus.apply, "status").list
  }

  def getFriends(): XmlResult[TwitterUser] = getFriends(TwitterArgs())
  def getFriends(cursor: Long): XmlResult[TwitterUser] = getFriends(TwitterArgs.cursor(cursor))
  def getFriendsFor(screenName: String)(cursor: Long): XmlResult[TwitterUser] = 
      getFriends(csrSnArgs(screenName, cursor))
  def getFriends(args: TwitterArgs): XmlResult[TwitterUser] =  
      parse("/statuses/friends.xml" + args, TwitterUser.apply, "users", "user")
  
  /** Builds TwitterArgs from screenName and cursor */
  private def csrSnArgs(screenName: String, cursor: Long) = 
      (if (screenName != user) TwitterArgs.screenName(screenName) else TwitterArgs()).cursor(cursor)
  
  def getFollowers(): XmlResult[TwitterUser] = getFollowers(TwitterArgs())
  def getFollowers(cursor: Long): XmlResult[TwitterUser] = getFollowers(TwitterArgs.cursor(cursor))
  def getFollowersFor(screenName: String)(cursor: Long): XmlResult[TwitterUser] = 
      getFollowers(csrSnArgs(screenName, cursor))
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
  
  def getDirectMessages(): List[TwitterMessage] = getDirectMessages(TwitterArgs())

  def getDirectMessages(args: TwitterArgs): List[TwitterMessage] = {
    parse("/direct_messages.xml" + args, TwitterMessage.apply, "direct_message").list
  }

  def getSentMessages(): List[TwitterMessage] = {
    parse("/direct_messages/sent.xml", TwitterMessage.apply, "direct_message").list
  }
  
  def getSentMessages(page: Int): List[TwitterMessage] = getSentMessages(TwitterArgs.page(page))

  def createList(listName: String): TwitterList = {
    TwitterList(http.post(url(user, "lists.xml"), List(("name", listName))))
  }
  
  def getLists(screenName: String): List[TwitterList] = extractLists(http.get(url(screenName, "lists.xml")))
  
  def getListMemberships(screenName: String)(cursor: Long): XmlResult[TwitterList] = 
    parse("/" + screenName + "/lists/memberships.xml?count=200&cursor=" + cursor, 
      TwitterList.apply, "lists", "list")
      
  def getListNamed(listName: String): Option[TwitterList] = getLists(user) find(_.name == listName)
  
  def getListMembers(list: TwitterList)(cursor: Long): XmlResult[TwitterUser] = {
    parse("/" + list.owner.screenName + "/" + list.slug + "/members.xml?count=200&cursor=" + cursor, 
      TwitterUser.apply, "users", "user")
  }
  
  /**
   * Gets (or creates if it doesn’t exit) a list and its members.
   */
  def getListAndMembers(listName: String): Tuple2[TwitterList, List[TwitterUser]] = {
    getListNamed(listName) match { 
      case Some(list) => (list, loadAllWithCursor(getListMembers(list)))
      case None => (createList(listName), List[TwitterUser]())
    }
  }

  def addToList(list: TwitterList)(memberId: Long): Node = http.post(listMembersUrl(list, memberId))

  def deleteFromList(list: TwitterList)(memberId: Long): Node = http.delete(listMembersUrl(list, memberId))
  
  private def listMembersUrl(list: TwitterList, memberId: Long) = 
    url(user, list.slug, "members.xml?id=" + memberId)
  
  private def extractLists(xml: Node): List[TwitterList] = ((xml \\ "list") map(TwitterList.apply)).toList

  def getSentMessages(args: TwitterArgs): List[TwitterMessage] = {
    parse("/direct_messages/sent.xml" + args, TwitterMessage.apply, "direct_message").list
  }

  def getFriendshipExists(id1: String, id2: String): Boolean = {
    val xml = http.get(url("friendships/exists.xml?user_a=" + urlEncode(id1) + "&user_b=" + urlEncode(id2)))
    xml match {
      case <friends>true</friends> => true
      case _ => false
    }
  }
  
  def verifyCredentials(): Boolean = {
    try{
      http.get(url("account/verify_credentials.xml"))
      return true
    } catch {
      case e:TwitterNotAuthorized => false
      case e => throw e
    }
  }
  
  def getArchive(): List[TwitterStatus] = {
    parse("/account/archive.xml", TwitterStatus.apply, "status").list
  }

  def getArchive(page: Int): List[TwitterStatus] = {
    parse("/account/archive.xml?page=" + page, TwitterStatus.apply, "status").list
  }
  
  def updateStatus(status: String): TwitterStatus = {
    TwitterStatus(http.post(url("statuses/update.xml?source=talkingpuffin"), List(("status", status))))
  }

  def updateStatus(status: String, statusId: Long): TwitterStatus = {
    TwitterStatus(http.post(url("statuses/update.xml?source=talkingpuffin"), List(("status", status), ("in_reply_to_status_id", statusId.toString()))))
  }
  def destroyStatus(statusId: Long): TwitterStatus = {
    TwitterStatus(http.delete(url("statuses/destroy/" + statusId + ".xml")))
  }

  /**
   *  @param recipient the user id <i>or</i> user name to send the message to 
   * @param text the body of the message
   */
  def newDirectMessage(recipient: String, text: String): TwitterMessage = {
    TwitterMessage(http.post(url("direct_messages/new.xml"), List(("user", recipient), ("text", text))))
  }
  
  def destroyDirectMessage(messageId: Long): TwitterMessage = {
    TwitterMessage(http.delete(url("direct_messages/destroy/" + messageId.toString() + ".xml")))
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to create a friendship to
  */
  def createFriendship(friendId: String): TwitterUser = {
    TwitterUser(http.post(url("friendships/create/" + friendId + ".xml")))
  }
  
  /**
  * @param friendId the user id <i>or</i> user name to destroy a friendship with
  */
  def destroyFriendship(friendId: String): TwitterUser = {
    TwitterUser(http.delete(url("friendships/destroy/" + friendId + ".xml")))
  }
  
  def updateLocation(location: String): TwitterUser = {
    TwitterUser(http.post(url("account/update_location.xml"), List(("location", location))))
  }

  def updateDeliveryService(device: String): TwitterUser = {
    TwitterUser(http.post(url("account/update_delivery_device.xml"), List(("device", device))))
  }
  
  def createFavorite(statusId: Long): TwitterStatus = {
    TwitterStatus(http.post(url("favorites/create/" + statusId + ".xml")))
  }

  def destroyFavorite(statusId: Long): TwitterStatus = {
    TwitterStatus(http.delete(url("favorites/destroy/" + statusId + ".xml")))
  }
  
  def followNotifications(userId: String): TwitterUser = {
    TwitterUser(http.post(url("notifications/follow/" + userId + ".xml")))
  }

  def leaveNotifications(userId: String): TwitterUser = {
    TwitterUser(http.post(url("notifications/leave/" + userId + ".xml")))
  }

  def blockUser(userId: String): TwitterUser = {
    TwitterUser(http.post(url("blocks/create/" + userId + ".xml")))
  }

  def unblockUser(userId: String): TwitterUser = {
    TwitterUser(http.post(url("blocks/destroy/" + userId + ".xml")))
  }

  def reportSpam(userId: String): TwitterUser = {
    TwitterUser(http.post(url("report_spam.xml"), List(("screen_name", userId.toString()))))
  }

  def getFriendIds(id:String) = {
    parse("/friends/ids/" + id + ".xml", (node: Node) => node.text.toLong, "id").list
  }

  def getFollowerIds(id:String) = {
    parse("/followers/ids/" + id + ".xml", (node: Node) => node.text.toLong, "id").list
  }

  def retweet(id:Long) = {
    TwitterStatus(http.post(url("statuses/retweet/" + id + ".xml?source=talkingpuffin")))
  }

  override protected def getHttp = http
}


