package com.davebsoft.sctw.filter

import _root_.scala.xml.Node
import ui.LinkExtractor

/**
 * Logic to do filtering, given a FilterSet, and collections of statuses and
 * filtered statuses.
 * @author Dave Briccetti
 */

class FilterLogic(username: String, filterSet: FilterSet, 
    filteredStatuses: java.util.List[Node]) {
  
  def filter(statuses: List[Node]) {
    filteredStatuses.clear
    for (st <- statuses) {
      var id = (st \ "user" \ "id").text
      if (! filterSet.mutedUsers.contains(id)) {
        if (tagFiltersInclude(id)) {
          val text = (st \ "text").text 
          if (! excludedBecauseReplyAndNotToYou(text)) {
            if (! filterSet.excludedByStringMatches(text)) {
              filteredStatuses.add(st)
            }
          }
        }
      }
    }
  }
    
  private def tagFiltersInclude(id: String): Boolean = {
    if (filterSet.selectedTags.length == 0) true else {
      for (tag <- filterSet.selectedTags) {
        if (com.davebsoft.sctw.filter.TagUsers.contains(new TagUser(tag, id))) {
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
  
