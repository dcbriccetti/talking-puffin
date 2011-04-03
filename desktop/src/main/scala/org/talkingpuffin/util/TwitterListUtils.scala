package org.talkingpuffin.util

import org.talkingpuffin.apix.PageHandler._
import twitter4j.{UserList, Twitter, User}

object TwitterListUtils extends Loggable {
  /**
   * Exports the specified tag and users to a Twitter list.
   */
  def exportTagToList(tw: Twitter, tag: String, description: String, userIds: List[Int]) {
    val lists: List[UserList] = allPages(userLists(tw, tw.getScreenName)).toList

    val list: UserList = changeOrCreateList(lists, tag, description, tw)

    val members: List[User] = allPages(userListMembers(tw, tw.getScreenName, list.getId))
    debug("Count: " + list.getMemberCount + " members.size: " + members.size)
    val listMemberIds: List[Int] = members.map(_.getId)
    def delMem(listId: Int)(userId: Int) = tw.deleteUserListMember(listId, userId)
    Parallelizer.run(10, listMemberIds -- userIds, delMem(list.getId), "Export tag")
    tw.addUserListMembers(list.getId, (userIds -- listMemberIds).toArray)
  }

  private def changeOrCreateList(lists: List[UserList], tag: String, description: String, tw: Twitter): UserList = {
    lists.find(_.getName == tag) match {
      case Some(list) =>
        if (list.getDescription != description) {
          tw.updateUserList(list.getId, tag, true, description)
        }
        list
      case None => tw.createUserList(tag, false, description)
    }
  }

}