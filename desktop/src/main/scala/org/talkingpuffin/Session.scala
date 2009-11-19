package org.talkingpuffin

import filter.{TagUsers}
import java.util.prefs.Preferences
import swing.Label
import java.awt.{Dimension, Toolkit}
import twitter.{AuthenticatedSession}
import ui.{TweetDetailPanel, DataProviders, Windows, LongOpListener}
import javax.swing.{JInternalFrame, JDesktopPane}

class Session(val serviceName: String, val twitterSession: AuthenticatedSession) {
  val tweetDetailPanel = new TweetDetailPanel(this, None) // TODO Some(filtersDialog))
  val desktopPane = new JDesktopPane {
    setDragMode(JDesktopPane.OUTLINE_DRAG_MODE)
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    val desktopSize = new Dimension(screenSize.width * 4 / 5, screenSize.height * 4 / 5)
    setPreferredSize(desktopSize)
    add(new JInternalFrame("Status Details", true, false, false, true) {
      setLayer(10)
      setContentPane(tweetDetailPanel.peer)
      pack
      setLocation(desktopSize.width / 2 - tweetDetailPanel.preferredSize.width / 2, 
        desktopSize.height / 2 - tweetDetailPanel.preferredSize.height / 2)
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

