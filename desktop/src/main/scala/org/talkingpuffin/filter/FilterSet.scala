package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher
import org.talkingpuffin.ui.{Relationships}
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

      tagFiltersInclude && ! excludedByTags && 
          ! (excludeFriendRetweets && status.isRetweetOfStatusFromFriend(friendUsernames)) &&
          ! (excludeNonFollowers && ! rels.followerIds.contains(status.user.id)) &&
          ! (useNoiseFilters && NoiseFilter.isNoise(status.text)) &&
          ! excludedByCompoundFilters
    }

    statuses.filter(includeStatus)
  }
  
  def muteApps(apps: List[String]) {
    apps.foreach(app => excludeSet.cpdFilters.add(
        CompoundFilter(List(SourceTextFilter(app, false)), None, None)))
    publish
  }

  def muteSenders(senders: List[String]) {
    senders.foreach(sender => excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sender, false)), None, None)))
    publish
  }

  def muteRetweetUsers(senders: List[String]) {
    senders.foreach(sender => excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sender, false)), Some(true), None)))
    publish
  }

  def muteSelectedUsersCommentedRetweets(senders: List[String]) {
    senders.foreach(sender => {
      excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sender, false)), Some(true), None))
      excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sender, false)), None, Some(true)))
    })
    publish
  }

  def muteSenderReceivers(srs: List[(String, String)]) {
    srs.foreach(sr => excludeSet.cpdFilters.add(
        CompoundFilter(List(FromTextFilter(sr._1, false), ToTextFilter(sr._2, false)), None, None)))
    publish
  }

}

case class FilterSetChanged(filterSet: FilterSet) extends Event

