package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser

trait PeoplePaneCreator {
  def createPeoplePane(longTitle: String, shortTitle: String, users: Option[List[TwitterUser]], 
      updatePeople: Option[() => Unit], selectPane: Boolean): PeoplePane
}
