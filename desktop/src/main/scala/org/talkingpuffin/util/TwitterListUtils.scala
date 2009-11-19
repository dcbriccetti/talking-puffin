package org.talkingpuffin.util

import org.talkingpuffin.twitter.{TwitterList, TwitterUser, AuthenticatedSession}

object TwitterListUtils extends Loggable {
  def export(tsess: AuthenticatedSession, tag: String, description: String, users: List[Long]): Unit = {
    val (list, members) = tsess.getListAndMembers(tag) match {
      case Some((list, members)) =>
        if (list.description != description) {
          tsess.changeListDescription(list, description)
        }
        (list, members) 
      case None => (tsess.createList(tag, description), List[TwitterUser]())
    }
    debug("Count: " + list.memberCount + ", members length: " + members.length)
    val currentUsers = members.map(_.id.toLong)
    Parallelizer.run(10, currentUsers -- users, tsess.deleteFromList(list))
    Parallelizer.run(10, users -- currentUsers, tsess.addToList(list))
  }
}