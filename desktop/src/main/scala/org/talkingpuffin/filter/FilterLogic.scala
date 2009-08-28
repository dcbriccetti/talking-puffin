package org.talkingpuffin.filter

import ui.LinkExtractor
import org.talkingpuffin.twitter.{TwitterStatus}

class FilterLogic(username: String, tagUsers: TagUsers, filterSet: FilterSet) {
  
  def filter(statuses: List[TwitterStatus]) = statuses.filter(filterStatus)

  private def filterStatus(status: TwitterStatus): Boolean = {
    val userId = status.user.id.toString()
    if (! filterSet.mutedUsers.contains(userId)) {
      if (! (filterSet.retweetMutedUsers.contains(userId) && status.text.toLowerCase.startsWith("rt"))) {
        if (tagFiltersInclude(userId)) {
          if (! filterSet.excludedByStringMatches(status.text)) {
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
  
