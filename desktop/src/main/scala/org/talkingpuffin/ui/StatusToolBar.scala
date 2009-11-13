package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import javax.swing.{JToolBar, JToggleButton, JFrame, SwingUtilities}
import scala.swing.{Label, Action}
import org.talkingpuffin.Session
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.ui.filter.FiltersDialog
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

  val clearAction = new Action("Clr") {
    toolTip = "Removes all tweets (except filtered-out ones) from the view"
    mnemonic = KeyEvent.VK_C
    def apply = clearAndOptionallyLoad(false)
  }

  val clearAllAction = new Action("Clr All") {
    toolTip = "Removes all tweets (including filtered-out ones) from the view"
    mnemonic = KeyEvent.VK_L
    def apply = clearAndOptionallyLoad(true)
  }

  val loadNewAction = new Action("Load New") {
    toolTip = "Loads any new items"
    mnemonic = KeyEvent.VK_N
    def apply = tweetsProvider.loadContinually()
  }
  
  val last200Action = new Action("200") {
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

  setFloatable(false)
  addComponentsToToolBar
  
  private def addComponentsToToolBar {
    aa(showFiltersAction, clearAction, clearAllAction, loadNewAction)
    aa(last200Action, wordsAction)
    addSeparator
    add(new Label("Cols: ").peer)
    aa(showMinColsAction, showMaxColsAction)
    addSeparator
    ac((new CommonToolbarButtons).createDetailsButton(tweetDetailPanel))
  }
  
  private def clearAndOptionallyLoad(all: Boolean) {
    clearTweets(all)
    if (GlobalPrefs.isOn(PrefKeys.NEW_AFTER_CLEAR))
      tweetsProvider.loadContinually()
  }
}

