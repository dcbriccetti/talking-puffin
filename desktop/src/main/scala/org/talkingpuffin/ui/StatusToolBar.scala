package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import javax.swing.{JToolBar, JToggleButton, JFrame, SwingUtilities}
import scala.swing.{Label, Action}
import org.talkingpuffin.Session
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import util.{ToolBarHelpers}

/**
 * Status pane tool bar
 */
class StatusToolBar(val session: Session, tweetsProvider: BaseProvider, filtersDialog: FiltersDialog, 
    val statusPane: StatusPane, showWordFrequencies: => Unit, clearTweets: (Boolean) => Unit, 
    showMaxColumns: (Boolean) => Unit) extends {
      val pane = statusPane
    } with JToolBar with ToolBarHelpers {
  var tweetDetailPanel: TweetDetailPanel = _
  
  val showFiltersAction = new Action("Filter") {
    toolTip = "Set filters for this stream"
    mnemonic = KeyEvent.VK_F
    def apply = {
      filtersDialog.peer.setLocationRelativeTo(SwingUtilities.getAncestorOfClass(classOf[JFrame], 
        statusPane.peer))
      filtersDialog.visible = true
    }
  }

  val clearAction = new Action("Clear") {
    toolTip = "Removes all tweets (except filtered-out ones) from the view"
    mnemonic = KeyEvent.VK_C
    def apply = clearAndOptionallyLoad(false)
  }

  val clearAllAction = new Action("Clear All") {
    toolTip = "Removes all tweets (including filtered-out ones) from the view"
    mnemonic = KeyEvent.VK_L
    def apply = clearAndOptionallyLoad(true)
  }

  val sendAction = new Action("Send") {
    toolTip = "Opens a window from which you can send a tweet"
    mnemonic = KeyEvent.VK_S
    def apply = (new SendMsgDialog(session, null, None, None, None, false)).visible = true
  }

  val dmAction = new Action("DM") {
    toolTip = "Opens a window from which you can send a direct message"
    mnemonic = KeyEvent.VK_D
    def apply = (new SendMsgDialog(session, null, None, None, None, true)).visible = true
  }

  val loadNewAction = new Action("Load New") {
    toolTip = "Loads any new items"
    mnemonic = KeyEvent.VK_N
    def apply = tweetsProvider.loadNewData
  }
  
  val last200Action = new Action("Last 200") {
    toolTip = "Fetches the last 200 items"
    mnemonic = KeyEvent.VK_2
    def apply = tweetsProvider.loadLastBlockOfTweets
  }

  val wordsAction = new Action("Words") {
    toolTip = "Shows word frequencies"
    mnemonic = KeyEvent.VK_W
    def apply = showWordFrequencies
  }

  val showMinColsAction = new Action("Min") {
    toolTip = "Show the minimum number of columns"
    mnemonic = KeyEvent.VK_M
    def apply = showMaxColumns(false)
  }

  val showMaxColsAction = new Action("Max") {
    toolTip = "Show the maximum number of columns"
    mnemonic = KeyEvent.VK_X
    def apply = showMaxColumns(true)
  }

  var detailsButton: JToggleButton = _ 
  val showDetailsAction = new Action("Details") {
    toolTip = "Shows or hides the details panel"
    def apply = tweetDetailPanel.visible = detailsButton.isSelected
  }
  detailsButton = new JToggleButton(showDetailsAction.peer)
  detailsButton.setSelected(true)

  setFloatable(false)
  addComponentsToToolBar
  
  private def addComponentsToToolBar {
    aa(sendAction, dmAction, showFiltersAction, clearAction, clearAllAction, loadNewAction)
    aa(last200Action, wordsAction)
    addSeparator
    add(new Label("Cols: ").peer)
    aa(showMinColsAction, showMaxColsAction)
    addSeparator
    ac(statusPane.dockedButton, detailsButton)
  }
  
  private def clearAndOptionallyLoad(all: Boolean) {
    clearTweets(all)
    if (GlobalPrefs.isOn(PrefKeys.NEW_AFTER_CLEAR))
      tweetsProvider.loadNewData
  }
}

