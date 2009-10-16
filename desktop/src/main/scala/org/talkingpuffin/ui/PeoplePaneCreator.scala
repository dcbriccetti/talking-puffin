package org.talkingpuffin.ui

import org.talkingpuffin.twitter.TwitterUser

trait PeoplePaneCreator {
  def createPeoplePane(users: Option[List[TwitterUser]], updatePeople: () => Unit): Unit
}
