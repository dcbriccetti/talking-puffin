package org.talkingpuffin.filter

import org.talkingpuffin.util.Loggable
import org.talkingpuffin.twitter.TwitterStatus

class DuplicatesFilter extends Loggable {
  private var ids = Set[Long]()
  def filter(statuses: List[TwitterStatus]): List[TwitterStatus] = {
    val filtered = statuses.filter(st => ! ids.contains(st.id))
    val dups = statuses.length - filtered.length
    if (dups > 0) debug("Removing " + dups + " tweets already seen")
    statuses.foreach(ids += _.id)
    filtered
  }
}
