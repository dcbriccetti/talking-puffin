package org.talkingpuffin.snippet

import org.talkingpuffin.user.UserAnalysis
import net.liftweb.http.SessionVar
import twitter4j.Twitter

object SessionState {
  object loggedIn     extends SessionVar[Boolean](false)
  object twitter      extends SessionVar[Option[Twitter]](None)
  object userAnalysis extends SessionVar[Option[UserAnalysis]](None)
}
