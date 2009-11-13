package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser
import java.awt.Rectangle

trait PeoplePaneCreator {
  def createPeoplePane(longTitle: String, otherRels: Option[Relationships],
      users: Option[List[TwitterUser]],
      updatePeople: Option[() => Unit], selectPane: Boolean, location: Option[Rectangle]): PeoplePane
}
