package com.davebsoft.sctw.twitter

import _root_.scala.xml.{NodeSeq, Node}

/**
 * Friends and followers data providers
 * @author Dave Briccetti
 */
abstract class FriendsFollowersDataProvider(username: String, password: String) extends DataProvider {
  setCredentials(username, password)
  
  def getUsers: List[Node] = {
    var usersList = List[Node]()
    var page = 1
    var users: NodeSeq = null
    
    do {
      val elem = loadTwitterData(getUrl + "?page=" + page)
      if (elem != null) {
        users = elem \ "user"
      
        for (user <- users) {
          usersList ::= user
        }
        page += 1
      }
    } while (users.length > 0)
    usersList
  }
}

class FriendsDataProvider(username: String, password: String) 
    extends FriendsFollowersDataProvider(username, password) {

  def getUrl = "http://twitter.com/statuses/friends.xml"
}

class FollowersDataProvider(username: String, password: String) 
    extends FriendsFollowersDataProvider(username, password) {

  def getUrl = "http://twitter.com/statuses/followers.xml"
}

