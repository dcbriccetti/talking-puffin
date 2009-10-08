package org.talkingpuffin.ui.web

import java.io.Serializable
import org.talkingpuffin.twitter.{AuthenticatedSession,TwitterUser,TwitterArgs}
import org.talkingpuffin.ui.{UserSelection, Relationships, UsersModel}

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

/**
 * Users managed bean.
 */
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
    val rels = new Relationships() {
      friends   = session.getFriends  (TwitterArgs.maxResults(200))
      followers = session.getFollowers(TwitterArgs.maxResults(200))
    }
    UsersModel(rels, UserSelection(true, true, None)).users.toArray
  }
}