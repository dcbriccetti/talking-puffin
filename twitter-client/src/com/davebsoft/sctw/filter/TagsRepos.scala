package com.davebsoft.sctw.filter

/**
 * Repository of tags. This will be customizable and persistent.
 * @author Dave Briccetti
 */
object TagsRepos {
  def get = List("Important", "Medium Priority", "Low Priority", "Family", "Friend", "Work",
    "Group1", "Group2", "Group3")
}