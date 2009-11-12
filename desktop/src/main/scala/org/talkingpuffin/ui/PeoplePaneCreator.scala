package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser
import java.awt.Point

trait PeoplePaneCreator {
  def createPeoplePane(longTitle: String, otherRels: Option[Relationships],
      users: Option[List[TwitterUser]],
      updatePeople: Option[() => Unit], selectPane: Boolean, location: Option[Point]): PeoplePane
}
