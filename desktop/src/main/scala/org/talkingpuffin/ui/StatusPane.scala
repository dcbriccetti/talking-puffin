package org.talkingpuffin.ui

import java.awt.{Dimension}
import javax.swing.event._
import swing.{ScrollPane, GridBagPanel}
import org.talkingpuffin.filter.{TagUsers, FilterSet}
import org.talkingpuffin.twitter.TwitterStatus
import org.talkingpuffin.Session
import util.{Dockable}

/**
 * Displays friend statuses
 */
class StatusPane(val session: Session, val longTitle: String, val shortTitle: String, 
    statusTableModel: StatusTableModel, 
    filterSet: FilterSet, tagUsers: TagUsers, viewCreator: ViewCreator) 
    extends GridBagPanel with TableModelListener with PreChangeListener with Dockable {
  var table: StatusTable = _
  private var lastSelectedRows: List[TwitterStatus] = Nil
  private var lastRowSelected: Boolean = _
  private val filtersDialog = new FiltersDialog(longTitle, statusTableModel, filterSet, tagUsers)

  statusTableModel.addTableModelListener(this)
  statusTableModel.preChangeListener = this
  
  val statusToolBar = new StatusToolBar(session, statusTableModel.tweetsProvider, 
    filtersDialog, this, showWordCloud, clearTweets, showMaxColumns)
  peer.add(statusToolBar, new Constraints{grid=(0,0); gridwidth=3}.peer)
  
  add(new ScrollPane {
    table = newTable
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  private val cursorSetter = new AfterFilteringCursorSetter(table)
  
  private val tweetDetailPanel = new TweetDetailPanel(session, table, filtersDialog, tagUsers, 
    viewCreator, viewCreator.prefs)
  add(tweetDetailPanel, new Constraints{
    grid = (0,3); fill = GridBagPanel.Fill.Horizontal;
  })
  
  statusToolBar.tweetDetailPanel = tweetDetailPanel
  
  table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(e: ListSelectionEvent) = {
      if (! e.getValueIsAdjusting) {
        if (table.getSelectedRowCount == 1) {
          try {
            val modelRowIndex = table.convertRowIndexToModel(table.getSelectedRow)
            val status = statusTableModel.getStatusAt(modelRowIndex)
            tweetDetailPanel.showStatusDetails(status)
            prefetchAdjacentRows        
          } catch {
            case ex: IndexOutOfBoundsException => println(ex)
          }
        } else {
          tweetDetailPanel.clearStatusDetails
        }
      }
    }
  })

  def saveState = table.saveState
  
  private def prefetchAdjacentRows {        
    List(-1, 1).foreach(offset => {
      val adjacentRowIndex = table.getSelectedRow + offset
      if (adjacentRowIndex >= 0 && adjacentRowIndex < table.getRowCount) {
        tweetDetailPanel.prefetch(statusTableModel.getStatusAt(
          table.convertRowIndexToModel(adjacentRowIndex)))
      }
    })
  }
  
  def newTable = new StatusTable(session, statusTableModel, showBigPicture)
  
  def showBigPicture = tweetDetailPanel.showBigPicture
  
  def tableChanging = {
    lastRowSelected = false
    cursorSetter.discardCandidates
    lastSelectedRows = table.getSelectedStatuses
    if (lastSelectedRows.length > 0) {
      val lastStatus = statusTableModel.getStatusAt(table.convertRowIndexToModel(table.getRowCount-1))
      if (lastSelectedRows contains lastStatus) {
        lastRowSelected = true
      }
      cursorSetter.captureTableState
    }
  }

  def tableChanged(e: TableModelEvent) = {
    if (table != null && e.getFirstRow != e.getLastRow) {
      val selectionModel = table.getSelectionModel
      selectionModel.clearSelection
      var numRestoredFromPrevSel = 0
      
      for (rowIndex <- 0 until table.getRowCount) {
        val status = statusTableModel.getStatusAt(table.convertRowIndexToModel(rowIndex))
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
    clearSelection
    statusTableModel.clear(all)
    tweetDetailPanel.clearStatusDetails
  }

  private def showWordCloud {
    new WordFrequenciesFrame(statusTableModel.filteredStatuses.map(_.text).mkString(" ")) {
      size = new Dimension(400, 400)
      peer.setLocationRelativeTo(null)
      visible = true
    }
  }
  
  private def showMaxColumns(showMax: Boolean) =
    statusTableModel.unessentialCols.foreach(table.getColumnExt(_).setVisible(showMax))
  
  private def clearSelection {
    table.getSelectionModel.clearSelection
    lastSelectedRows = Nil
  }
  
  def requestFocusForTable = table.requestFocusInWindow
}
