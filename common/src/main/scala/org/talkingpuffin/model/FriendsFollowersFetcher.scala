package org.talkingpuffin.model

import org.talkingpuffin.apix.PageHandler._
import org.talkingpuffin.util.Threads
import twitter4j.{Twitter, User}

object FriendsFollowersFetcher {
  type Users = List[User]

  def getUsers(tw: Twitter, screenName: Option[String] = None): FriendsFollowers = {
    val name = screenName.getOrElse(tw.getScreenName)
    val friendsFuture   = Threads.submitCallable {allPages(friendsStatuses  (tw, name))}
    val followersFuture = Threads.submitCallable {allPages(followersStatuses(tw, name))}
    FriendsFollowers(friendsFuture.get, followersFuture.get)
  }
}
