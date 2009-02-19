package com.davebsoft.sctw.filter

/**
 * Repository of tags. This will be customizable and persistent.
 * @author Dave Briccetti
 */
object tagsRepository {
  def get = List("Important", "Chatty", "Family", "Friend", "Work")
}