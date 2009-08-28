package org.talkingpuffin.filter

import ui.LinkExtractor
import org.talkingpuffin.twitter.{TwitterStatus}

/**
 * Logic to do filtering, given a FilterSet, and collections of statuses and
 * filtered statuses.
 */
class FilterLogic(username: String, tagUsers: TagUsers, filterSet: FilterSet) {
  
  def filter(statuses: List[TwitterStatus]) = statuses.filter(filterStatus)

  private def filterStatus(st: TwitterStatus): Boolean = {
    val userId = st.user.id.toString()
    if (! filterSet.mutedUsers.contains(userId)) {
      if (tagFiltersInclude(userId)) {
        val text = st.text
        if (! filterSet.excludedByStringMatches(text)) {
          if (! filterSet.excludedByOverlap(userId)) {
            return true
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
}
  
