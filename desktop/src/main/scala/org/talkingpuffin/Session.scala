package org.talkingpuffin

import java.util.prefs.Preferences
import swing.Label
import javax.swing.JDesktopPane
import filter.TagUsers
import twitter.AuthenticatedSession
import ui.{DataProviders, Windows, LongOpListener}
import java.awt.{Dimension, Toolkit}

class Session(val serviceName: String, val twitterSession: AuthenticatedSession) {
  val desktopPane = new JDesktopPane {
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    setPreferredSize(new Dimension(screenSize.width * 4 / 5, screenSize.height * 4 / 5))
  }
  val windows = new Windows
  val statusMsgLabel = new Label(" ")
  def statusMsg_=(text: String) = statusMsgLabel.text = text
  def statusMsg = statusMsgLabel.text
  var progress: LongOpListener = null
  var dataProviders: DataProviders = _
  def userPrefs: Preferences = windows.streams.prefs
  def tagUsers: TagUsers = windows.streams.tagUsers
}

