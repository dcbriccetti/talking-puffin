package org.talkingpuffin.web

import java.io.Serializable
import twitter4j.{User, Twitter}
import org.talkingpuffin.apix.RichUser._
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.model.{UserSelection, UsersModel, BaseRelationships}

class UserRow(val picUrl: String, val arrows: String, val screenName: String, val name: String,
    val numFriends: Int, val numFollowers: Int,
    val location: String, val description: String, val status: String) extends Serializable

class Users extends Serializable {
  var tw: Twitter = _
  val rels = new BaseRelationships

  def setSession(session: Twitter) = this.tw = session

  def getUsers: Array[User] = {
    rels.lookUp(tw)
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
  }
}
