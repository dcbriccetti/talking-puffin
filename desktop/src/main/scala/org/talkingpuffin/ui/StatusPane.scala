package org.talkingpuffin.ui

import _root_.scala.swing.event.{ComponentResized, ButtonClicked}
import _root_.scala.swing.GridBagPanel._
import _root_.org.talkingpuffin.util.PopupListener

import apache.log4j.Logger
import filter.{FilterSet}
import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.awt.event.{KeyEvent, KeyAdapter}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import javax.swing.{SwingUtilities, JTable, Icon, JMenu, ImageIcon, JLabel, JTextPane, SwingWorker, JPopupMenu, JFrame, JToolBar, JToggleButton, JButton, JMenuItem, JTabbedPane}
import scala.swing._
import twitter.{TwitterStatus}
import util.TableUtil

/**
 * Displays friend statuses
 */
class StatusPane(session: Session, title: String, statusTableModel: StatusTableModel, 
    filterSet: FilterSet, streams: Streams) 
    extends GridBagPanel with TableModelListener with PreChangeListener {
  private val log = Logger.getLogger("StatusPane " + hashCode)
  var table: StatusTable = _
  private var lastSelectedRows: List[TwitterStatus] = Nil
  private var lastRowSelected: Boolean = _
  /** IDs of statuses following the cursored status, for choosing cursored status after filtering */
  private val filtersDialog = new FiltersDialog(title, statusTableModel, filterSet, streams.tagUsers)

  statusTableModel.addTableModelListener(this)
  statusTableModel.setPreChangeListener(this)
  
  val statusToolBar = new StatusToolBar(session, statusTableModel.tweetsProvider, 
    filtersDialog, this, clearTweets, showMaxColumns)
  peer.add(statusToolBar, new Constraints{grid=(0,0); gridwidth=3}.peer)
  
  add(new ScrollPane {
    table = newTable
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  private val cursorSetter = new AfterFilteringCursorSetter(table)
  
  private val tweetDetailPanel = new TweetDetailPanel(session, table, filtersDialog, streams)
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
    log.debug("Before table change. Selected rows: " + lastSelectedRows.size + 
        ", last selected? " + lastRowSelected)
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
          log.debug("Selecting last row, " + i)
          selectionModel.addSelectionInterval(i, i)
        } else {
          cursorSetter.setCursor
        }
      }
    }
  }
  
  private def clearTweets {
    clearSelection
    statusTableModel.clear
    tweetDetailPanel.clearStatusDetails
  }
  
  private def showMaxColumns(showMax: Boolean) {
    for (i <- List("Age","From","To"))
      table.getColumnExt(i).setVisible(showMax);
  }
  
  private def clearSelection {
    table.getSelectionModel.clearSelection
    lastSelectedRows = Nil
  }
  
  def requestFocusForTable = table.requestFocusInWindow
}


