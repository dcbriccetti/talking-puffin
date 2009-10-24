package org.talkingpuffin

import java.util.prefs.Preferences
import twitter.AuthenticatedSession
import ui.{Windows, LongOpListener}
import swing.Label

class Session(val serviceName: String, val twitterSession: AuthenticatedSession) {
  val windows = new Windows
  val statusMsgLabel = new Label(" ")
  def statusMsg_=(text: String) = statusMsgLabel.text = text
  def statusMsg = statusMsgLabel.text
  var progress: LongOpListener = null
  def userPrefs: Preferences = windows.streams.prefs
}

