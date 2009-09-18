package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.collection.mutable.LinkedHashMap
import _root_.scala.swing.Publisher
import java.util.regex.Pattern
import twitter.TwitterStatus
import ui.User

/**
 * A set of all filters, and logic to apply them
 */
class FilterSet(session: Session, username: String, tagUsers: TagUsers) extends Publisher {
  val mutedUsers = LinkedHashMap[Long,User]()
  val retweetMutedUsers = LinkedHashMap[Long,User]()
  var selectedTags = List[String]()
  var excludeFriendRetweets: Boolean = false
  var excludeNonFollowers: Boolean = false
  var includeTextFilters = new TextFilters()
  var excludeTextFilters = new TextFilters()
  
  def publish: Unit = publish(new FilterSetChanged(this))

  /**
   * Filter the given list of statuses, returning a list of only those that pass the filters
   * in this set.
   */
  def filter(statuses: List[TwitterStatus], friendUsernames: List[String], followerIds: List[Long]) = 
    statuses.filter(includeStatus(friendUsernames, followerIds))

  private def includeStatus(friendUsernames: List[String], followerIds: List[Long])(status: TwitterStatus): Boolean = {
    val userId = status.user.id
    ! mutedUsers.contains(userId) &&
        ! (retweetMutedUsers.contains(userId) && status.text.toLowerCase.startsWith("rt @")) &&
        tagFiltersInclude(userId) && 
        ! (excludeFriendRetweets && Retweets.fromFriend_?(status.text, friendUsernames)) &&
        ! (excludeNonFollowers && ! followerIds.contains(status.user.id)) &&
        ! excludedByStringMatches(status.text)
  }
  
  private def tagFiltersInclude(userId: Long) = selectedTags == Nil ||
    selectedTags.exists(tagUsers.contains(_, userId)) 

  private def excludedByStringMatches(text: String): Boolean = 
    (includeTextFilters.list.length > 0 && ! includeTextFilters.list.exists(matches(text, _))) ||
        excludeTextFilters.list.exists(matches(text, _))
  
  private def matches(text: String, search: TextFilter): Boolean = if (search.isRegEx) 
    Pattern.compile(search.text).matcher(text).find else text.toUpperCase.contains(search.text.toUpperCase)
}

class TextFilter (var text: String, var isRegEx: Boolean)

case class FilterSetChanged(filterSet: FilterSet) extends Event

class TextFilters {
  var list = List[TextFilter]()
  def clear = list = List[TextFilter]() 
}