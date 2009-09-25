package org.talkingpuffin.ui.web

import _root_.scala.xml.NodeSeq
import java.io.Serializable
import twitter.{AuthenticatedSession,TwitterUser,TwitterArgs}
import ui.UsersModel

/**
 * Users managed bean.
 */
class UserRow(val picUrl: String, val arrows: String, val screenName: String, val name: String,
    val numFriends: Int, val numFollowers: Int,
    val location: String, val description: String, val status: String) extends Serializable {
  def getPicUrl = picUrl
  def getArrows = arrows
  def getScreenName = screenName
  def getName = name
  def getNumFriends = numFriends
  def getNumFollowers = numFollowers
  def getLocation = location
  def getDescription = description
  def getStatus = status
}

class Users extends Serializable {
  var session: AuthenticatedSession = _
  def setSession(session: AuthenticatedSession) = this.session = session
  var following:List[TwitterUser] = _
  var followers:List[TwitterUser] = _

  def getArrows(user:TwitterUser) = {
    val friend = following.contains(user)
    val follower = followers.contains(user)
    if (friend && follower) "↔" else if (friend) "→" else "←"
  }

  def getUsers: Array[TwitterUser] = {
    //TODO: make sure we get more than 20 here
    val rels = new Relationships()
    rels.friends = session.getFriends(TwitterArgs.maxResults(200))
    rels.followers = session.getFollowers(TwitterArgs.maxResults(200))
    val usersModel = new UsersModel(rels)
    usersModel.build(UserSelection(true, true, None))
    usersModel.users.toArray
  }
}