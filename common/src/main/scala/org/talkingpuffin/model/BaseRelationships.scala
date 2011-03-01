package org.talkingpuffin.model

import java.util.concurrent.{Callable, Executors}
import org.talkingpuffin.apix.PageHandler._
import twitter4j.{Twitter, User}

class BaseRelationships {
  type Users = List[User]

  var friendIds = List[Long]()
  var followerIds = List[Long]()
  var friends = List[User]()
  var followers = List[User]()

  def getUsers(tw: Twitter): Tuple2[Users,Users] = {
    val pool = Executors.newFixedThreadPool(2)
    val name = tw.getScreenName
    val friendsFuture   = pool.submit(new Callable[Users] { def call = {allPages(friendsStatuses  (tw, name))}})
    val followersFuture = pool.submit(new Callable[Users] { def call = {allPages(followersStatuses(tw, name))}})
    val result = (friendsFuture.get, followersFuture.get)
    pool.shutdown()
    result
  }

  def lookUp(tw: Twitter) {
    val fol = tw.getAccountTotals.getFollowers
    val fr = tw.getAccountTotals.getFriends
    if (fol > 2000 || fr > 2000)
      throw new TooManyFriendsFollowers
    val ff = getUsers(tw)
    friends = ff._1
    followers = ff._2
  }

}

class TooManyFriendsFollowers extends Exception
