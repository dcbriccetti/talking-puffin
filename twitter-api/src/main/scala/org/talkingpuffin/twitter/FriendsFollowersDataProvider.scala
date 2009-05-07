package org.talkingpuffin.twitter

import _root_.scala.xml.{NodeSeq, Node}

/**
 * Friends and followers data providers
 * @author Dave Briccetti
 */
abstract class FriendsFollowersDataProvider(username: String, password: String) extends DataProvider {
  setCredentials(username, password)
  
  def getUsers: List[Node] = {
    getUsers(1)
  }

  def getUsers(page:Int): List[Node] = {
    loadTwitterData(getUrl + "?page=" + page) match {
      case HttpXMLSuccess(_,_,n) => {
          parseUsers(n) match {
            case Nil => Nil
            case l => l ::: getUsers(page+1)
          }
      }
      case _ => Nil
    }
  }

  def parseUsers(x:Node) = {
    var usersList = List[Node]()
    for (user <- x \ "user") {
      usersList ::= user
    }
    usersList
  }
}

class FriendsDataProvider(username: String, password: String) 
    extends FriendsFollowersDataProvider(username, password) {

  def getUrl = urlHost + "statuses/friends.xml"
}

class FollowersDataProvider(username: String, password: String) 
    extends FriendsFollowersDataProvider(username, password) {

  def getUrl = urlHost + "statuses/followers.xml"
}

