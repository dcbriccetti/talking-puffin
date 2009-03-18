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
  
  def publish: Unit = publish(new FilterSetChanged(this))
}

case class FilterSetChanged(filterSet: FilterSet) extends Event