package org.talkingpuffin

import org.talkingpuffin.filter.{TagUsers}
import java.util.prefs.Preferences
import ui._
import twitter4j.Twitter
import util.Loggable
import swing.{TabbedPane, Label}

class Session(val serviceName: String, val twitter: Twitter, tabbed: Boolean = true) extends Loggable {
  val desktopPane: MainContents = if (tabbed) new TopTabbedPane(this) else new DesktopPane(this)
  var streams: Streams = _
  var peoplePaneCreator: PeoplePaneCreator = _
  val statusMsgLabel = new Label(" ")
  var progress: LongOpListener = null
  var dataProviders: DataProviders = _
  def userPrefs: Preferences = streams.prefs
  def tagUsers: TagUsers = streams.tagUsers

  /**
   * Records an error message for display to the user.
   */
  def addMessage(msg: String): Unit = {
    // TODO  expand this into a feature that presents all accumulated error messages
    info(msg)
    SwingInvoke.later(statusMsgLabel.text = msg)
  }
  
  def clearMessage() = SwingInvoke.later(statusMsgLabel.text = " ")
}

