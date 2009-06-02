package org.talkingpuffin.twitter

import _root_.scala.xml.{NodeSeq, Node}

/**
 * Friends and followers data providers
 */
abstract class FriendsFollowersDataProvider(session: AuthenticatedSession){
  def getUsers: List[TwitterUser] = {
    getUsers(1)
  }

  def getUsers(page:Int): List[TwitterUser]
}

class FriendsDataProvider(session: AuthenticatedSession)
    extends FriendsFollowersDataProvider(session) {

  def getUsers(page: Int) = session.getFriends(page)
}

class FollowersDataProvider(session: AuthenticatedSession)
    extends FriendsFollowersDataProvider(session) {

  def getUsers(page: Int) = session.getFollowers(page)
}

