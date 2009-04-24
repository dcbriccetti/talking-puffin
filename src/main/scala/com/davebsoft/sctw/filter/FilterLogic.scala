package com.davebsoft.sctw.filter

import _root_.scala.xml.Node
import ui.LinkExtractor

/**
 * Logic to do filtering, given a FilterSet, and collections of statuses and
 * filtered statuses.
 * @author Dave Briccetti
 */

class FilterLogic(username: String, filterSet: FilterSet, filteredStatuses: java.util.List[Node]) {
  
  def filter(statuses: List[Node]) {
    filteredStatuses.clear
    for (st <- statuses if filterStatus(st)) {
      filteredStatuses.add(st)
    }
  }

  private def filterStatus(st: Node): Boolean = { 
    val userId = (st \ "user" \ "id").text
    if (! filterSet.mutedUsers.contains(userId)) {
      if (tagFiltersInclude(userId)) {
        val text = (st \ "text").text 
        if (! excludedBecauseReplyAndNotToYou(text)) {
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
  
  private def tagFiltersInclude(userId: String): Boolean = {
    if (filterSet.selectedTags.length == 0) true else {
      for (tag <- filterSet.selectedTags) {
        if (TagUsers.contains(new TagUser(tag, userId))) {
          return true
        }
      }
      false
    }
  }
    
  private def excludedBecauseReplyAndNotToYou(text: String): Boolean = {
    if (! filterSet.excludeNotToYouReplies) return false

    LinkExtractor.getReplyToUser(text) match {
      case Some(user) => ! user.equals(username)
      case None => false
    }
  }
}
  
