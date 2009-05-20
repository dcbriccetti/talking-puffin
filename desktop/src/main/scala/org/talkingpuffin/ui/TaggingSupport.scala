package org.talkingpuffin.ui

import filter.TagUsers

/**
 * Adds tagging support to table models.
 */
trait TaggingSupport {
  val tagUsers: TagUsers
  
  def getUsers(rows: List[Int]): List[User] 

  def tagSelectedUsers(rows: List[Int], tag: String) =
    for (user <- getUsers(rows)) 
      tagUsers.add(tag, user.id)

  def untagSelectedUsers(rows: List[Int]) =
    for (user <- getUsers(rows)) 
      tagUsers.removeForUser(user.id)
}
