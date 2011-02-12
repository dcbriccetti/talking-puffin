package org.talkingpuffin.util

import org.talkingpuffin.twitter.{AuthenticatedSession}
import twitter4j.{Twitter, User}

object TwitterListUtils extends Loggable {
  /**
   * Exports the specified tag and users to a Twitter list.
   */
  def exportTagToList(tsess: Twitter,
      tag: String, description: String, users: List[Long]): Unit = {}/*todo
    val (list, members) = tsess.getListAndMembers(tag) match {
      case Some((list, members)) =>
        if (list.description != description) {
          tsess.changeListDescription(list, description)
        }
        (list, members) 
      case None => (tsess.createList(tag, description), List[User]())
    }
    debug("Count: " + list.memberCount + ", members length: " + members.length)
    val listMembers = members.map(_.id.toLong)
    Parallelizer.run(10, listMembers -- users, tsess.deleteFromList(list))
    Parallelizer.run(10, users -- listMembers, tsess.addToList(list))
  }*/
}