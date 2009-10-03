package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser

/**
 * A model for users (followed and followers)
 */
class UsersModel(rels: Relationships) {
  var users: Array[TwitterUser] = _
  var arrows: Array[String] = _
  var screenNameToUserNameMap = Map[String, String]()
  var friendScreenNames: Set[String] = _

  def build(sel: UserSelection) {
    var set = scala.collection.mutable.Set[TwitterUser]()
    if (sel.includeFollowing) set ++ selected(rels.friends  , sel.searchString)
    if (sel.includeFollowers) set ++ selected(rels.followers, sel.searchString)
    val combinedList = set.toList.sort((a,b) => 
      (a.name.toLowerCase compareTo b.name.toLowerCase) < 0)
    users = combinedList.toArray
    arrows = combinedList.map(user => {
      val friend = rels.friends.contains(user)
      val follower = rels.followers.contains(user)
      if (friend && follower) "↔" else if (friend) "→" else "←"
    }).toArray
    screenNameToUserNameMap = 
        Map(users map {user => (user.screenName, user.name)} : _*)
        friendScreenNames = Set(rels.friends map {f => f.screenName} : _*)
  }
  
  def selected(users: List[TwitterUser], search: Option[String]) = search match {
    case None => users
    case Some(search) => users.filter(u => u.name.toLowerCase.contains(search.toLowerCase))
  }
}

