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

  private def nodeToNum(node: NodeSeq, name: String): Int = {
    val s = (node \ name).text
    var num = 0
    try {
      num = Integer.parseInt(s)
    } catch {
      case e: NumberFormatException =>
    }
    num
  }
  
  def getUsers: Array[TwitterUser] = {
    //TODO: make sure we get more than 20 here
    val following = session.getFriends(TwitterArgs.maxResults(200))
    val followers = session.getFollowers(TwitterArgs.maxResults(200))
    val usersModel = new UsersModel(following, followers)
    usersModel.build(UserSelection(true, true, None))
    usersModel.users.reverse.toArray
  }
}