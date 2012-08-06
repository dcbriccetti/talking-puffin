package org.talkingpuffin.filter

import swing.Publisher
import swing.event.Event
import twitter4j.Status

/**
 * A list of CompoundFilters, and methods to operate on them.
 */
class CompoundFilters extends Publisher {
  var list = List[CompoundFilter]()

  /**
   * Clears the list of filters.
   */
  def clear() {
    list = List[CompoundFilter]()
  }

  /**
   * Returns whether the given status matches all the filters.
   */
  def matchesAll(status: Status) = list.forall(_.matches(status))
  
  /**
   * Returns whether the given status matches any of the filters.
   */
  def matchesAny(status: Status) = list.exists(_.matches(status))

  /**
   * Adds a filter to the list
   */
  def add(compoundFilter: CompoundFilter) {
    list = list ::: List(compoundFilter)
    publish()
  }

  /**
   * Publishes that the filters have changed.
   */
  def publish() {
    publish(new CompoundFiltersChanged)
  }
  
  override def toString = list.map(_.toString).mkString("↑")
}

class CompoundFiltersChanged extends Event
