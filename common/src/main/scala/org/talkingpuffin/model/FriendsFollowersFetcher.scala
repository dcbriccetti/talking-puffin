package org.talkingpuffin.model

import java.util.concurrent.{Callable, Executors}
import org.talkingpuffin.apix.PageHandler._
import twitter4j.{Twitter, User}

object FriendsFollowersFetcher {
  type Users = List[User]

  def getUsers(tw: Twitter, screenName: Option[String] = None): FriendsFollowers = {
    val pool = Executors.newFixedThreadPool(2)
    val name = screenName.getOrElse(tw.getScreenName)
    val friendsFuture   = pool.submit(new Callable[Users] { def call = {allPages(friendsStatuses  (tw, name))}})
    val followersFuture = pool.submit(new Callable[Users] { def call = {allPages(followersStatuses(tw, name))}})
    val result = FriendsFollowers(friendsFuture.get, followersFuture.get)
    pool.shutdown()
    result
  }
}
