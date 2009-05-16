package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher
import java.util.regex.Pattern
import java.util.{ArrayList,Collections}
import ui.User

/**
 * A set of all filters
 */

class TextFilter (var text: String, var isRegEx: Boolean)

class FilterSet(session: Session) extends Publisher {
  val mutedUsers = scala.collection.mutable.LinkedHashMap[String,User]()
  var selectedTags = List[String]()
  var excludeNotToFollowingReplies: Boolean = _
  var excludeOverlapping: Boolean = _
  val includeTextFilters = Collections.synchronizedList(new ArrayList[TextFilter]())
  val excludeTextFilters = Collections.synchronizedList(new ArrayList[TextFilter]())
  
  def excludedByStringMatches(text: String): Boolean = {
    if (includeTextFilters.size() == 0 && excludeTextFilters.size() == 0) return false
    
    if (includeTextFilters.size > 0) {
      var numMatches = 0
      for (i <- 0 until includeTextFilters.size) {
        if (matches(text, includeTextFilters.get(i))) numMatches += 1
      }
      if (numMatches == 0) return true
    }

    for (i <- 0 until excludeTextFilters.size) {
      if (matches(text, excludeTextFilters.get(i))) return true
    }
    
    false
  }
  
  def excludedByOverlap(userId: String): Boolean = {
    if (! excludeOverlapping) return false
    for (aSession <- Globals.sessions if aSession != session) {
      aSession.windows.streams.usersTableModel.friends.foreach(friend => {
        val friendId = (friend \ "id").text
        if (friendId == userId) 
          return true
      })
    }
    false
  }
  
  def friendScreenNames = session.windows.streams.usersTableModel.usersModel.friendScreenNames // TODO simplify

  private def matches(text: String, search: TextFilter): Boolean = if (search.isRegEx) 
    Pattern.matches(search.text, text) else text.toUpperCase.contains(search.text.toUpperCase)

  def publish: Unit = publish(new FilterSetChanged(this))
}

case class FilterSetChanged(filterSet: FilterSet) extends Event