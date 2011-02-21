package org.talkingpuffin.ui

import java.awt.Rectangle
import twitter4j.User

trait PeoplePaneCreator {
  def createPeoplePane(longTitle: String, shortTitle: String, otherRels: Option[Relationships],
      users: Option[List[User]],
      updatePeople: Option[() => Unit], location: Option[Rectangle]): PeoplePane
}
