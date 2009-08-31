package org.talkingpuffin.filter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher
import java.util.regex.Pattern
import ui.User

/**
 * A set of all filters
 */
class FilterSet(session: Session) extends Publisher {
  val mutedUsers = scala.collection.mutable.LinkedHashMap[String,User]()
  val retweetMutedUsers = scala.collection.mutable.LinkedHashMap[String,User]()
  var selectedTags = List[String]()
  var includeTextFilters = new TextFilters()
  var excludeTextFilters = new TextFilters()
  
  def excludedByStringMatches(text: String): Boolean = 
    (includeTextFilters.list.length > 0 && ! includeTextFilters.list.exists(matches(text, _))) ||
        excludeTextFilters.list.exists(matches(text, _))
  
  private def matches(text: String, search: TextFilter): Boolean = if (search.isRegEx) 
    Pattern.matches(search.text, text) else text.toUpperCase.contains(search.text.toUpperCase)

  def publish: Unit = publish(new FilterSetChanged(this))
}

class TextFilter (var text: String, var isRegEx: Boolean)

case class FilterSetChanged(filterSet: FilterSet) extends Event

class TextFilters {
  var list = List[TextFilter]()
  def clear = list = List[TextFilter]() 
}