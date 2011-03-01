package org.talkingpuffin.web

import java.io.Serializable
//import org.talkingpuffin.ui.{UserSelection, UsersModel}
import twitter4j.{User, Twitter}

class UserRow(val picUrl: String, val arrows: String, val screenName: String, val name: String,
    val numFriends: Int, val numFollowers: Int,
    val location: String, val description: String, val status: String) extends Serializable

class Users extends Serializable {
  /*
  var session: Twitter = _
  val rels = new Relationships

  def setSession(session: Twitter) = this.session = session

  def getUsers: Array[User] = {
    rels.friends   = session.loadAllWithCursor(session.getFriends)
    rels.followers = session.loadAllWithCursor(session.getFollowers)
    UsersModel(None, rels, UserSelection(true, true, None)).users.toArray
  }
  
  def getStatus(user: User): String = {
    user.status match {
      case Some(ts) => ts.text
      case _ => ""
    }
  }
  
  def getArrows(user: User): String = {
    val friend = rels.friends.contains(user)
    val follower = rels.followers.contains(user)
    if (friend && follower) "↔" else if (friend) "→" else if (follower) "←" else " "
  }*/
}