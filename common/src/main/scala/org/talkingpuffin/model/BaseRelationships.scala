package org.talkingpuffin.model

import twitter4j.{Twitter, User}

class BaseRelationships {
  var friendIds   = List[Long]()
  var followerIds = List[Long]()
  var friends     = List[User]()
  var followers   = List[User]()

  def lookUp(tw: Twitter) {
    val fol = tw.getAccountTotals.getFollowers
    val fr  = tw.getAccountTotals.getFriends
    if (fol > 2000 || fr > 2000)
      throw new TooManyFriendsFollowers
    val ff = FriendsFollowersFetcher.getUsers(tw)
    friends   = ff.friends
    followers = ff.followers
  }

}

class TooManyFriendsFollowers extends Exception
