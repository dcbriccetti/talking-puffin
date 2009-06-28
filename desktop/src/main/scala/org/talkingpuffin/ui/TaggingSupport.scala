package org.talkingpuffin.ui

import filter.TagUsers

/**
 * Adds tagging support to table models.
 */
trait TaggingSupport {
  val tagUsers: TagUsers
  
  def getUsers(rows: List[Int]): List[User]

  /**
   * If all selected users have the same tags applied, return those tags, otherwise return an 
   * empty list.
   */
  def tagsForSelectedUsers(selectedRows: List[Int]): List[String] = {
    val users = getUsers(selectedRows)
    val firstUserTags = tagUsers.tagsForUser(users(0).id)
    val firstUserTagsSet = Set(firstUserTags: _*)
    var i = 1
    while (i < users.length) {
      val userTags = Set(tagUsers.tagsForUser(users(i).id): _*)
      if (userTags != firstUserTagsSet) return List[String]()
      i += 1
    }
    firstUserTags
  }
  
  def tagSelectedUsers(rows: List[Int], tag: String) =
    for (user <- getUsers(rows)) 
      tagUsers.add(tag, user.id)

  def untagSelectedUsers(rows: List[Int]) =
    for (user <- getUsers(rows)) 
      tagUsers.removeForUser(user.id)
}
