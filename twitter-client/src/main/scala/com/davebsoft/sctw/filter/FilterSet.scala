package com.davebsoft.sctw.filter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher
import java.util.Collections
import ui.User

/**
 * A set of all filters
 * @author Dave Briccetti
 */

class TextFilter (var text: String, var isRegEx: Boolean)

class FilterSet extends Publisher {
  val mutedUsers = scala.collection.mutable.LinkedHashMap[String,User]()
  var selectedTags = List[String]()
  var excludeNotToYouReplies: Boolean = _
  var includeTextFilters = Collections.synchronizedList(new java.util.ArrayList[TextFilter]())
  var excludeTextFilters = Collections.synchronizedList(new java.util.ArrayList[TextFilter]())
  
  def excludedByStringMatches(text: String): Boolean = {
    if (includeTextFilters.size() == 0 && excludeTextFilters.size() == 0) return false
    
    if (includeTextFilters.size > 0) {
      var numMatches = 0
      for (i <- 0 until includeTextFilters.size) {
        if (matches(text, includeTextFilters.get(i))) numMatches += 1
      }
      if (numMatches == 0) return true
    }

    for (i <- 0 until excludeTextFilters.size) {
      if (matches(text, excludeTextFilters.get(i))) return true
    }
    
    false
  }
  
  private def matches(text: String, search: TextFilter): Boolean = {
    if (search.isRegEx) {
      java.util.regex.Pattern.compile(search.text).matcher(text).find
    } else {
      text.contains(search.text)
    }
  }

  def publish: Unit = publish(new FilterSetChanged(this))
}

case class FilterSetChanged(filterSet: FilterSet) extends Event