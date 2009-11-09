package org.talkingpuffin.filter

import scala.swing.event.Event

/**
 * The provided FilterSet has changed.
 */
case class FilterSetChanged(filterSet: FilterSet) extends Event
