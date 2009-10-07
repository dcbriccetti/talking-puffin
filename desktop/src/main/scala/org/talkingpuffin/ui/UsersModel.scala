package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser

/**
 * A model for users (followed and followers)
 */
class UsersModel(rels: Relationships, val users: Array[TwitterUser], val arrows: Array[String],
    val screenNameToUserNameMap: Map[String, String], val friendScreenNames: Set[String])

object UsersModel {

  def apply(rels: Relationships, sel: UserSelection): UsersModel = {

    def selected(users: List[TwitterUser], search: Option[String]) = search match {
      case None => users
      case Some(search) => users.filter(u => u.name.toLowerCase.contains(search.toLowerCase))
    }

    val combinedList = {
      var set = Set[TwitterUser]()
      if (sel.includeFriends)   set ++= selected(rels.friends  , sel.searchString)
      if (sel.includeFollowers) set ++= selected(rels.followers, sel.searchString)
      set.toList.sort(_.name.toLowerCase < _.name.toLowerCase)
    }
    val users = combinedList.toArray
    val arrows = combinedList.map(user => {
      val friend = rels.friends.contains(user)
      val follower = rels.followers.contains(user)
      if (friend && follower) "↔" else if (friend) "→" else "←"
    }).toArray
    val screenNameToUserNameMap = Map(users map {user => (user.screenName, user.name)} : _*)
    val friendScreenNames = Set(rels.friends map {f => f.screenName} : _*)
    new UsersModel(rels, users, arrows, screenNameToUserNameMap, friendScreenNames)
  }
}

