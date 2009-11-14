package org.talkingpuffin

import java.util.prefs.Preferences
import swing.Label
import filter.TagUsers
import twitter.AuthenticatedSession
import java.awt.{Dimension, Toolkit}
import ui.{TweetDetailPanel, DataProviders, Windows, LongOpListener}
import javax.swing.{JInternalFrame, JDesktopPane}

class Session(val serviceName: String, val twitterSession: AuthenticatedSession) {
  val tweetDetailPanel = new TweetDetailPanel(this, None) // TODO Some(filtersDialog))
  val desktopPane = new JDesktopPane {
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    val desktopSize = new Dimension(screenSize.width * 4 / 5, screenSize.height * 4 / 5)
    setPreferredSize(desktopSize)
    add(new JInternalFrame("Status Details", true, false, false, true) {
      setContentPane(tweetDetailPanel.peer)
      pack
      setLocation(0, desktopSize.height - tweetDetailPanel.preferredSize.height)
      setVisible(true)
    })
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

