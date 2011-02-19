package org.talkingpuffin.twitter

import twitter4j.{User, Status}

/**
 * The twitter4j.User class, augmented with additional features for application
 * and Scala suitability.
 */
case class RichUser(user: User) extends OptionMaker {
  def status: Option[Status] = makeOption(user.getStatus)
  def location = makeEmptyString(user.getLocation)
  def description = makeEmptyString(user.getDescription)
}

object RichUser {
  implicit def asRichUser(user: User): RichUser = RichUser(user)
}