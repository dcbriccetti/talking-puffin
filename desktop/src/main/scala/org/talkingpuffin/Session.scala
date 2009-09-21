package org.talkingpuffin

import java.util.prefs.Preferences
import twitter.AuthenticatedSession
import ui.{Windows, LongOpListener}
import swing.Label

class Session(val serviceName: String, val twitterSession: AuthenticatedSession) {
  val windows = new Windows
  val status = new Label(" ")
  var progress: LongOpListener = null
  def userPrefs: Preferences = windows.streams.prefs
}

