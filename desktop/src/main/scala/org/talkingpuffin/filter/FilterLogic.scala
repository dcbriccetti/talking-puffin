package org.talkingpuffin.filter

import _root_.scala.xml.Node
import ui.LinkExtractor
import org.talkingpuffin.twitter.{TwitterStatus}

/**
 * Logic to do filtering, given a FilterSet, and collections of statuses and
 * filtered statuses.
 */

class FilterLogic(username: String, tagUsers: TagUsers, filterSet: FilterSet, 
  filteredStatuses: java.util.List[TwitterStatus]) {
  
  def filter(statuses: List[TwitterStatus]) {
    filteredStatuses.clear
    for (st <- statuses if filterStatus(st)) {
      filteredStatuses.add(st)
    }
  }

  private def filterStatus(st: TwitterStatus): Boolean = {
    val userId = st.user.id.toString()
    if (! filterSet.mutedUsers.contains(userId)) {
      if (tagFiltersInclude(userId)) {
        val text = st.text
        if (! excludedBecauseReplyToStranger(text)) {
          if (! filterSet.excludedByStringMatches(text)) {
            if (! filterSet.excludedByOverlap(userId)) {
              return true
            }
          }
        }
      }
    }
    false
  }
  
  private def tagFiltersInclude(userId: String) = filterSet.selectedTags.length match {
    case 0 => true
    case _ => filterSet.selectedTags.exists(tag => tagUsers.contains(tag, userId)) 
  }
    
  private def excludedBecauseReplyToStranger(text: String) = 
    filterSet.excludeNotToFollowingReplies match {
      case true =>
        LinkExtractor.getReplyToUser(text) match {
          case Some(user) => ! user.equals(username) && ! filterSet.friendScreenNames.contains(user)
          case None => false
        }
      case false => false
    }
}
  
