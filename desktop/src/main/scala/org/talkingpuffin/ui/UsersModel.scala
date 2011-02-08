package org.talkingpuffin.ui

import twitter4j.User

/**
 * A model for users (followed and followers)
 */
class UsersModel(rels: Relationships, val users: Array[User], val arrows: Array[String],
    val screenNameToUserNameMap: Map[String, String], val friendScreenNames: Set[String])

object UsersModel {

  /**
   * If users is None, the list of users will be built from the relationships.
   */
  def apply(usersList: Option[List[User]], rels: Relationships, sel: UserSelection): UsersModel = {

    def selected(users: List[User], search: Option[String]) = search match {
      case None => users
      case Some(search) => users.filter(u => u.getName.toLowerCase.contains(search.toLowerCase))
    }

    val combinedList = (usersList match {
      case Some(u) => u
      case None =>  
        var set = Set[User]()
        if (sel.includeFriends)   set ++= selected(rels.friends  , sel.searchString)
        if (sel.includeFollowers) set ++= selected(rels.followers, sel.searchString)
        set.toList
    }).sort(_.getName.toLowerCase < _.getName.toLowerCase)
    val users = combinedList.toArray
    val arrows = combinedList.map(user => {
      val friend = rels.friends.contains(user)
      val follower = rels.followers.contains(user)
      if (friend && follower) "↔" else if (friend) "→" else if (follower) "←" else " "
    }).toArray
    val screenNameToUserNameMap = Map(users map {user => (user.getScreenName, user.getName)} : _*)
    val friendScreenNames = Set(rels.friends map {f => f.getScreenName} : _*)
    new UsersModel(rels, users, arrows, screenNameToUserNameMap, friendScreenNames)
  }
}

