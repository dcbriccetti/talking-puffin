package org.talkingpuffin.ui

import _root_.scala.swing.{Component, Action, Key}
import java.awt.event.KeyEvent
import javax.swing.{JToolBar, JToggleButton, JFrame, SwingUtilities}

/**
 * Status pane tool bar
 * @author Dave Briccetti
 */
class StatusToolBar(session: Session, filtersDialog: FiltersDialog, apiHandlers: ApiHandlers, 
    statusPane: Component, clearTweets: => Unit) extends JToolBar {
  var tweetDetailPanel: TweetDetailPanel = _
  
  val showFiltersAction = new Action("Filter…") {
    toolTip = "Set filters for this stream"
    mnemonic = KeyEvent.VK_F
    def apply = {
      filtersDialog.peer.setLocationRelativeTo(SwingUtilities.getAncestorOfClass(classOf[JFrame], 
        statusPane.peer))
      filtersDialog.visible = true
    }
  }

  val clearAction = new Action("Clear") {
    toolTip = "Removes all tweets (including filtered-out ones)"
    mnemonic = KeyEvent.VK_C
    def apply = clearTweets
  }

  val sendAction = new Action("Send…") {
    toolTip = "Opens a window from which you can send a tweet"
    mnemonic = KeyEvent.VK_S
    def apply = (new SendMsgDialog(session, null, apiHandlers.sender, None, None, None)).visible = true
  }

  val clearRepliesAction = new Action("Clear") {
    toolTip = "Removes all mentions"
    mnemonic = KeyEvent.VK_C
    def apply = clearTweets
  }

  var detailsButton: JToggleButton = _ 
  val showDetailsAction = new Action("Details") {
    toolTip = "Shows or hides the details panel"
    def apply = tweetDetailPanel.visible = detailsButton.isSelected
  }
  detailsButton = new JToggleButton(showDetailsAction.peer)
  detailsButton.setSelected(true)

  var dockedButton: JToggleButton = _ 
  val dockedAction = new Action("Docked") {
    toolTip = "Docks or frees the pane"
    def apply = {
      if (! dockedButton.isSelected) {
        session.windows.undock(statusPane)
      } else {
        session.windows.dock(statusPane)
      }
    }
  }
  dockedButton = new JToggleButton(dockedAction.peer)
  dockedButton.setSelected(true)

  setFloatable(false)
  add(sendAction.peer)
  add(showFiltersAction.peer)
  add(clearAction.peer)
  addSeparator
  add(dockedButton)
  add(detailsButton)
}
  
