package com.davebsoft.sctw.twitter

import _root_.scala.xml.Node

/**
 * Friends and followers data providers
 * @author Dave Briccetti
 */
abstract class FriendsFollowersDataProvider(username: String, password: String) extends DataProvider {
  setCredentials(username, password)
  
  def getUsers: List[Node] = {
    val elem = loadTwitterData
    if (elem == null) {
      List[Node]()
    } else {
      val users = elem \ "user"
      var usersList = List[Node]()
      
      for (user <- users) {
        usersList ::= user
      }
    
      usersList.sort((a,b) => ((a \ "name").text.toLowerCase compareTo 
              (b \ "name").text.toLowerCase) < 0)
    }
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

