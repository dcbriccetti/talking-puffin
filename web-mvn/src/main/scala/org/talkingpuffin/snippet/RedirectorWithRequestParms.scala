package org.talkingpuffin.snippet

import net.liftweb.http._
import net.liftweb.common.Full

trait RedirectorWithRequestParms {
  def makeUserParm = S.param("user") match {
    case Full(user) => "?user=" + user
    case _ => ""
  }
}
