package org.talkingpuffin.web

import java.io.Serializable
import org.talkingpuffin.twitter.{AuthenticatedSession,TwitterUser,TwitterArgs}
import org.talkingpuffin.ui.{UserSelection, Relationships, UsersModel}

class UserRow(val picUrl: String, val arrows: String, val screenName: String, val name: String,
    val numFriends: Int, val numFollowers: Int,
    val location: String, val description: String, val status: String) extends Serializable

/**
 * Users managed bean.
 */
class Users extends Serializable {
  var session: AuthenticatedSession = _
  val rels = new Relationships

  def setSession(session: AuthenticatedSession) = this.session = session

  def getArrows(user:TwitterUser): String = {
    val friend = rels.friends.contains(user)
    val follower = rels.followers.contains(user)
    if (friend && follower) "↔" else if (friend) "→" else "←"
  }

  def getUsers: Array[TwitterUser] = {
    rels.friends   = session.getFriends  (TwitterArgs.maxResults(200)).list
    rels.followers = session.getFollowers(TwitterArgs.maxResults(200)).list
    UsersModel(rels, UserSelection(true, true, None)).users.toArray
  }
}