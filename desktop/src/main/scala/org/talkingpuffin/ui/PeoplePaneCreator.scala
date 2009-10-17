package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser

trait PeoplePaneCreator {
  def createPeoplePane(title: String, users: Option[List[TwitterUser]], 
      updatePeople: Option[() => Unit]): PeoplePane
}
