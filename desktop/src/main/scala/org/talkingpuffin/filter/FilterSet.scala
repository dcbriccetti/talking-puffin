package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.collection.mutable.LinkedHashMap
import _root_.scala.swing.Publisher
import org.talkingpuffin.ui.{Relationships, User}
import org.talkingpuffin.twitter.TwitterStatus
import org.talkingpuffin.filter.RetweetDetector._
import org.talkingpuffin.util.Loggable

/**
 * A set of all filters, and logic to apply them
 */
class FilterSet(tagUsers: TagUsers) extends Publisher with Loggable {
  class InOutSet {
    var cpdFilters = new CompoundFilters()
    var tags = List[String]()
    def tagMatches(userId: Long) = tags.exists(tagUsers.contains(_, userId))
  }
  val retweetMutedUsers = LinkedHashMap[Long,User]()
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
    
      def excludedByCompoundFilters: Boolean = {
        (includeSet.cpdFilters.list != Nil && !includeSet.cpdFilters.matchesAll(status)) ||
                excludeSet.cpdFilters.matchesAny(status)
      }

      ! (retweetMutedUsers.contains(status.user.id) && status.isRetweet) &&
          tagFiltersInclude && ! excludedByTags && 
          ! (excludeFriendRetweets && status.isFromFriend(friendUsernames)) &&
          ! (excludeNonFollowers && ! rels.followerIds.contains(status.user.id)) &&
          ! (useNoiseFilters && NoiseFilter.isNoise(status.text)) &&
          ! excludedByCompoundFilters
    }

    statuses.filter(includeStatus)
  }
  
  def muteApps(apps: List[String]) {
    apps.foreach(app => excludeSet.cpdFilters.add(CompoundFilter(None, None, None, 
      Some(SourceTextFilter(app, false)), None)))
    publish
  }

  def muteSenders(senders: List[String]) {
    senders.foreach(sender => excludeSet.cpdFilters.add(CompoundFilter(Some(FromTextFilter(sender, false)), 
      None, None, None, None)))
    publish
  }

  def muteRetweetUsers(senders: List[String]) {
    senders.foreach(sender => excludeSet.cpdFilters.add(CompoundFilter(Some(FromTextFilter(sender, false)), 
      None, None, None, Some(true))))
    publish
  }

  def muteSenderReceivers(srs: List[(String, String)]) {
    srs.foreach(sr => excludeSet.cpdFilters.add(CompoundFilter(Some(FromTextFilter(sr._1, false)), 
      None, Some(ToTextFilter(sr._2, false)), None, None)))
    publish
  }

}

case class FilterSetChanged(filterSet: FilterSet) extends Event

