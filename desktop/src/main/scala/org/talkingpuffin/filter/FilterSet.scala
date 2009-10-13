package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.collection.mutable.LinkedHashMap
import _root_.scala.swing.Publisher
import java.util.regex.Pattern
import org.talkingpuffin.ui.{Relationships, User}
import org.talkingpuffin.twitter.TwitterStatus
import org.talkingpuffin.filter.RetweetDetector._

/**
 * A set of all filters, and logic to apply them
 */
class FilterSet(tagUsers: TagUsers) extends Publisher {
  class InOutSet {
    var textFilters = new TextFilters()
    var tags = List[String]()
    def tagMatches(userId: Long) = tags.exists(tagUsers.contains(_, userId))
  }
  val mutedUsers = LinkedHashMap[Long,User]()
  val retweetMutedUsers = LinkedHashMap[Long,User]()
  val mutedApps = LinkedHashMap[Long,User]()
  var excludeFriendRetweets: Boolean = false
  var excludeNonFollowers: Boolean = false
  var useNoiseFilters: Boolean = false
  val includeSet = new InOutSet
  val excludeSet = new InOutSet
  
  def publish: Unit = publish(new FilterSetChanged(this))

  /**
   * Filter the given list of statuses, returning a list of only those that pass the filters
   * in this set.
   */
  def filter(statuses: List[TwitterStatus], rels: Relationships): List[TwitterStatus] = {
    val friendUsernames = rels.friends.map(_.screenName)
    
    def includeStatus(status: TwitterStatus): Boolean = {
      def tagFiltersInclude = includeSet.tags == Nil || includeSet.tagMatches(status.user.id)
      def excludedByTags = excludeSet.tagMatches(status.user.id)
    
      def excludedByStringMatches: Boolean = { 
        def matches(search: TextFilter) = 
          if (search.isRegEx) 
            Pattern.compile(search.text).matcher(status.text).find 
          else 
            status.text.toUpperCase.contains(search.text.toUpperCase)
      
        (includeSet.textFilters.list != Nil && ! includeSet.textFilters.list.exists(matches)) ||
            excludeSet.textFilters.list.exists(matches)
      }
  
      ! mutedUsers.contains(status.user.id) &&
          ! mutedApps.contains(status.sourceName.hashCode) &&
          ! (retweetMutedUsers.contains(status.user.id) && status.isRetweet) &&
          tagFiltersInclude && ! excludedByTags && 
          ! (excludeFriendRetweets && status.isFromFriend(friendUsernames)) &&
          ! (excludeNonFollowers && ! rels.followerIds.contains(status.user.id)) &&
          ! (useNoiseFilters && NoiseFilter.isNoise(status.text)) &&
          ! excludedByStringMatches
    }

    statuses.filter(includeStatus)
  }

}

class TextFilter (var text: String, var isRegEx: Boolean)

case class FilterSetChanged(filterSet: FilterSet) extends Event

class TextFilters {
  var list = List[TextFilter]()
  def clear = list = List[TextFilter]() 
}
