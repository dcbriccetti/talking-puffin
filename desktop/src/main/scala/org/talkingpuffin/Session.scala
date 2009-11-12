package org.talkingpuffin

import filter.TagUsers
import java.util.prefs.Preferences
import twitter.AuthenticatedSession
import swing.Label
import ui.{Windows, LongOpListener}

class Session(val serviceName: String, val twitterSession: AuthenticatedSession) {
  val windows = new Windows
  val statusMsgLabel = new Label(" ")
  def statusMsg_=(text: String) = statusMsgLabel.text = text
  def statusMsg = statusMsgLabel.text
  var progress: LongOpListener = null
  def userPrefs: Preferences = windows.streams.prefs
  def tagUsers: TagUsers = windows.streams.tagUsers
}

