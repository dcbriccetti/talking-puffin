package org.talkingpuffin.util

import org.talkingpuffin.twitter.AuthenticatedSession

object TwitterListUtils extends Loggable {
  def export(tsess: AuthenticatedSession, tag: String, users: List[Long]): Unit = {
    val (list, members) = tsess.getListAndMembers(tag)
    debug("Count: " + list.memberCount + ", members length: " + members.length)
    val currentUsers = members.map(_.id.toLong)
    Parallelizer.run(10, currentUsers -- users, tsess.deleteFromList(list))
    Parallelizer.run(10, users -- currentUsers, tsess.addToList(list))
  }
}