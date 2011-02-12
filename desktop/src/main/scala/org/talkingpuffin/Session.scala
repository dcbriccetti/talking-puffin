package org.talkingpuffin

import org.talkingpuffin.filter.{TagUsers}
import java.util.prefs.Preferences
import swing.Label
import java.awt.{Dimension, Toolkit}
import twitter.{AuthenticatedSession}
import javax.swing.{JInternalFrame, JDesktopPane}
import ui._
import twitter4j.Twitter
import util.Loggable

class Session(val serviceName: String, val twitter: Twitter) extends Loggable {
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
  var progress: LongOpListener = null
  var dataProviders: DataProviders = _
  def userPrefs: Preferences = windows.streams.prefs
  def tagUsers: TagUsers = windows.streams.tagUsers

  /**
   * Records an error message for display to the user.
   */
  def addMessage(msg: String): Unit = {
    // TODO  expand this into a feature that presents all accumulated error messages
    info(msg)
    SwingInvoke.later(statusMsgLabel.text = msg)
  }
  
  def clearMessage(): Unit = {
    SwingInvoke.later(statusMsgLabel.text = " ")
  }
}

