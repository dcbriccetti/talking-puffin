package com.davebsoft.sctw.filter

import scala.collection.mutable.Set

/**
 * Repository of tag -> user mappings
 * @author Dave Briccetti
 */

case class TagUser(userId: String, tag: String)

/**
 * A set of tag -> user pairings
 */
object tagUsers {
  private val tagUsers = Set[TagUser]()
  
  def add(tagUser: TagUser) {
    tagUsers += tagUser
  }
  
  def contains(tagUser: TagUser): Boolean = {
    return tagUsers.contains(tagUser)
  }
}