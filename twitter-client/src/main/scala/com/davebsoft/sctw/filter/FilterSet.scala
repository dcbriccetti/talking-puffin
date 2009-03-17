package com.davebsoft.sctw.filter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher

/**
 * A set of all filters
 * @author Dave Briccetti
 */

class TextFilter (var text: String, var isRegEx: Boolean)

class FilterSet extends Publisher {
  var selectedTags = List[String]()
  var excludeNotToYouReplies: Boolean = _
  var includeTextFilters = List[TextFilter]()
  var excludeTextFilters = List[TextFilter]()
}

case class FilterSetChanged(filterSet: FilterSet) extends Event