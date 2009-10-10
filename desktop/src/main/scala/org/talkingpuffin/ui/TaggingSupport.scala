package org.talkingpuffin.ui

import org.talkingpuffin.filter.TagUsers

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
    if (users.tail.forall(u => Set(tagUsers.tagsForUser(u.id): _*) == firstUserTagsSet)) 
      firstUserTags 
    else 
      List[String]() 
  }
  
  def tagSelectedUsers(rows: List[Int], tags: List[String]) = 
      getUsers(rows).foreach(user => tags.foreach(tag => tagUsers.add(tag, user.id)))

  def untagSelectedUsers(rows: List[Int]) = getUsers(rows).foreach(u => tagUsers.removeForUser(u.id))
}
