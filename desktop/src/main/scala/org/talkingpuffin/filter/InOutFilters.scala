package org.talkingpuffin.filter

/**
 * A set of filters used for including or excluding tweets.
 */
class InOutFilters(tagUsers: TagUsers) {
  val cpdFilters = new CompoundFilters
  var tags = List[String]()

  /**
   * Whether this filter set includes a tag that matches the user with 
   * the specified ID.
   */
  def tagMatches(userId: Long) = tags.exists(tagUsers.contains(_, userId))
}
  
