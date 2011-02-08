package org.talkingpuffin.twitter

import twitter4j.{User, Status}

case class RichUser(user: User) {
  def status: Option[Status] = user.getStatus match {
    case null => None
    case s => Some(s)
  }
}

object RichUser {
  implicit def asRichUser(user: User): RichUser = RichUser(user)
}