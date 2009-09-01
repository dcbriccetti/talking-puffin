package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher
import java.util.regex.Pattern
import twitter.TwitterStatus
import ui.User

/**
 * A set of all filters, and logic to apply them
 */
class FilterSet(session: Session, username: String, tagUsers: TagUsers) extends Publisher {
  val mutedUsers = scala.collection.mutable.LinkedHashMap[String,User]()
  val retweetMutedUsers = scala.collection.mutable.LinkedHashMap[String,User]()
  var selectedTags = List[String]()
  var includeTextFilters = new TextFilters()
  var excludeTextFilters = new TextFilters()
  
  def publish: Unit = publish(new FilterSetChanged(this))

  def filter(statuses: List[TwitterStatus]) = statuses.filter(includeStatus)

  private def includeStatus(status: TwitterStatus): Boolean = {
    val userId = status.user.id.toString()
    ! mutedUsers.contains(userId) &&
        ! (retweetMutedUsers.contains(userId) && 
        status.text.toLowerCase.startsWith("rt @")) &&
        tagFiltersInclude(userId) && 
        ! excludedByStringMatches(status.text)
  }
  
  private def tagFiltersInclude(userId: String) = if (selectedTags.length == 0) true else
    selectedTags.exists(tagUsers.contains(_, userId)) 

  private def excludedByStringMatches(text: String): Boolean = 
    (includeTextFilters.list.length > 0 && ! includeTextFilters.list.exists(matches(text, _))) ||
        excludeTextFilters.list.exists(matches(text, _))
  
  private def matches(text: String, search: TextFilter): Boolean = if (search.isRegEx) 
    Pattern.matches(search.text, text) else text.toUpperCase.contains(search.text.toUpperCase)
}

class TextFilter (var text: String, var isRegEx: Boolean)

case class FilterSetChanged(filterSet: FilterSet) extends Event

class TextFilters {
  var list = List[TextFilter]()
  def clear = list = List[TextFilter]() 
}