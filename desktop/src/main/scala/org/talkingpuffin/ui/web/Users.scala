package org.talkingpuffin.ui.web

import _root_.scala.xml.NodeSeq
import java.io.Serializable
import twitter.{AuthenticatedSession,TwitterUser,TwitterArgs}
import ui.UsersModel

/**
 * Users managed bean.
 * 
 * @author Dave Briccetti
 */

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