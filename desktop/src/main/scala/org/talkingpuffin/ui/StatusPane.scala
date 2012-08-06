package org.talkingpuffin.ui

import java.awt.Dimension
import javax.swing.event._
import swing.{ScrollPane, GridBagPanel}
import twitter4j.Status
import org.talkingpuffin.filter.{TagUsers, FilterSet}
import org.talkingpuffin.ui.filter.FiltersDialog
import org.talkingpuffin.Session
import util.Dockable

/**
 * Displays friend statuses
 */
class StatusPane(val session: Session, val longTitle: String, val shortTitle: String, tableModel: StatusTableModel,
    filterSet: FilterSet, tagUsers: TagUsers) 
    extends GridBagPanel with TableModelListener with PreChangeListener with Dockable {
  var table: StatusTable = _
  private var lastSelectedRows: List[Status] = Nil
  private var lastRowSelected: Boolean = _
  private val filtersDialog = new FiltersDialog(longTitle, tableModel, filterSet, tagUsers)

  tableModel.addTableModelListener(this)
  tableModel.preChangeListener = Some(this)
  
  val statusToolBar = new StatusToolBar(session, tableModel.tweetsProvider, 
    filtersDialog, this, showWordCloud(), clearTweets, showMaxColumns)
  peer.add(statusToolBar, new Constraints{grid=(0,0); gridwidth=3; anchor=GridBagPanel.Anchor.West}.peer)
  
  add(new ScrollPane {
    table = newTable
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1
  })
  
  private val cursorSetter = new AfterFilteringCursorSetter(table)
  
  private val tweetDetailPanel = new TweetDetailPanel(session, Some(filtersDialog))
  add(tweetDetailPanel, new Constraints{
    grid = (0,3); fill = GridBagPanel.Fill.Horizontal
  })

  statusToolBar.tweetDetailPanel = tweetDetailPanel

  tweetDetailPanel.connectToTable(table, Some(filtersDialog))

  def saveState() {
    table.saveState()
  }
  
  def newTable = new StatusTable(session, tableModel, tweetDetailPanel.showBigPicture())
  
  def tableChanging() {
    lastRowSelected = false
    cursorSetter.discardCandidates
    lastSelectedRows = table.getSelectedStatuses(retweets = false)
    if (lastSelectedRows.length > 0) {
      val lastStatus = tableModel.getStatusAt(table.convertRowIndexToModel(table.getRowCount-1))
      if (lastSelectedRows contains lastStatus) {
        lastRowSelected = true
      }
      cursorSetter.captureTableState
    }
  }

  def tableChanged(e: TableModelEvent) {
    if (table != null && e.getFirstRow != e.getLastRow) {
      val selectionModel = table.getSelectionModel
      selectionModel.clearSelection()
      var numRestoredFromPrevSel = 0
      
      for (rowIndex <- 0 until table.getRowCount) {
        val status = tableModel.getStatusAt(table.convertRowIndexToModel(rowIndex))
        if (lastSelectedRows.contains(status)) {
          selectionModel.addSelectionInterval(rowIndex, rowIndex)
          numRestoredFromPrevSel += 1
        }
      }
      
      if (numRestoredFromPrevSel == 0 && table.getRowCount > 0) {
        if (lastRowSelected) {
          val i = table.getRowCount - 1
          selectionModel.addSelectionInterval(i, i)
        } else {
          cursorSetter.setCursor
        }
      }
    }
  }
  
  private def clearTweets(all: Boolean) {
    clearSelection()
    tableModel.clear(all)
    tweetDetailPanel.clearStatusDetails()
  }

  private def showWordCloud() {
    new WordFrequenciesFrame(tableModel.filteredStatuses.map(_.getText).mkString(" ")) {
      size = new Dimension(400, 400)
      peer.setLocationRelativeTo(null)
      visible = true
    }
  }
  
  private def showMaxColumns(showMax: Boolean) {
    tableModel.unessentialCols.foreach(table.getColumnExt(_).setVisible(showMax))
  }
  
  private def clearSelection() {
    table.getSelectionModel.clearSelection()
    lastSelectedRows = Nil
  }
  
  def requestFocusForTable = table.requestFocusInWindow
}
