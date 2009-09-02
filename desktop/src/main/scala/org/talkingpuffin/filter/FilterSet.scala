package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.collection.mutable.LinkedHashMap
import _root_.scala.swing.Publisher
import java.util.regex.Pattern
import twitter.TwitterStatus
import ui.{LinkExtractor, User}

/**
 * A set of all filters, and logic to apply them
 */
class FilterSet(session: Session, username: String, tagUsers: TagUsers) extends Publisher {
  val mutedUsers = LinkedHashMap[String,User]()
  val retweetMutedUsers = LinkedHashMap[String,User]()
  var selectedTags = List[String]()
  var excludeFriendRetweets: Boolean = false
  var includeTextFilters = new TextFilters()
  var excludeTextFilters = new TextFilters()
  
  def publish: Unit = publish(new FilterSetChanged(this))

  /**
   * Filter the given list of statuses, returning a list of only those that pass the filters
   * in this set.
   */
  def filter(statuses: List[TwitterStatus], friendUsernames: List[String]) = statuses.filter(includeStatus(friendUsernames))

  private def includeStatus(friendUsernames: List[String])(status: TwitterStatus): Boolean = {
    val userId = status.user.id.toString()
    ! mutedUsers.contains(userId) &&
        ! (retweetMutedUsers.contains(userId) && status.text.toLowerCase.startsWith("rt @")) &&
        tagFiltersInclude(userId) && 
        retweetFriendsIncludes(status.text, friendUsernames) && 
        ! excludedByStringMatches(status.text)
  }
  
  private def tagFiltersInclude(userId: String) = if (selectedTags.length == 0) true else
    selectedTags.exists(tagUsers.contains(_, userId)) 

  private def excludedByStringMatches(text: String): Boolean = 
    (includeTextFilters.list.length > 0 && ! includeTextFilters.list.exists(matches(text, _))) ||
        excludeTextFilters.list.exists(matches(text, _))
  
  private def matches(text: String, search: TextFilter): Boolean = if (search.isRegEx) 
    Pattern.matches(search.text, text) else text.toUpperCase.contains(search.text.toUpperCase)
  
  private val rtUserRegex = ("(rt|♺) " + LinkExtractor.usernameRegex + ".*").r

  private def retweetFriendsIncludes(statusText: String, friendUsernames: List[String]): Boolean = {
    if (! excludeFriendRetweets) return true
    
    try {
      val rtUserRegex(rtSymbol, username) = statusText.toLowerCase
      return ! friendUsernames.contains(username)
    } catch {
      case e: MatchError => return true
    }
  }
}

class TextFilter (var text: String, var isRegEx: Boolean)

case class FilterSetChanged(filterSet: FilterSet) extends Event

class TextFilters {
  var list = List[TextFilter]()
  def clear = list = List[TextFilter]() 
}