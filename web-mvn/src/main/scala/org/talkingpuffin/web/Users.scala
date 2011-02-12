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
  var session: Twitter = _
  val rels = new Relationships

  def setSession(session: Twitter) = this.session = session

  def getUsers: Array[TwitterUser] = {
    rels.friends   = session.loadAllWithCursor(session.getFriends)
    rels.followers = session.loadAllWithCursor(session.getFollowers)
    UsersModel(None, rels, UserSelection(true, true, None)).users.toArray
  }
  
  def getStatus(user: TwitterUser): String = {
    user.status match {
      case Some(ts) => ts.text
      case _ => ""
    }
  }
  
  def getArrows(user: TwitterUser): String = {
    val friend = rels.friends.contains(user)
    val follower = rels.followers.contains(user)
    if (friend && follower) "↔" else if (friend) "→" else if (follower) "←" else " "
  }
}