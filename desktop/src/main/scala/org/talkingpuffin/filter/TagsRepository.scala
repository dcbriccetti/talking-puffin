package org.talkingpuffin.filter

/**
 * Repository of tags. This will be customizable and persistent.
 * @author Dave Briccetti
 */
object TagsRepository {
  def get = List("Important", "Medium Priority", "Low Priority", "Family", "Friend", "Work",
    "Group1", "Group2", "Group3")
}