package com.davebsoft.sctw.ui

import _root_.scala.swing.event.{ComponentResized, ButtonClicked}
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.xml.{NodeSeq, Node}

import filter.{TagsRepository, FilterSet}
import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.awt.event.{KeyEvent, KeyAdapter}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import javax.swing.{JTextPane, SwingWorker, JPopupMenu, JTable, JToolBar, JToggleButton, Icon, JMenu, JButton, JMenuItem, ImageIcon, JTabbedPane, JLabel}
import scala.swing._
import twitter.Sender
import util.TableUtil

/**
 * Displays friend statuses
 */
class StatusPane(title: String, statusTableModel: StatusTableModel, apiHandlers: ApiHandlers, 
    filterSet: FilterSet, streams: Streams) 
    extends GridBagPanel with TableModelListener with PreChangeListener {
  var table: StatusTable = _
  var lastSelectedRows: List[NodeSeq] = Nil
  val filtersDialog = new FiltersDialog(title, statusTableModel, filterSet)

  val showFiltersAction = new Action("Filter…") {
    toolTip = "Set filters for this stream"
    def apply {
      filtersDialog.visible = true
    }
  }
  val sendAction = new Action("Send…") {
    toolTip = "Opens a window from which you can send a tweet"
    def apply { 
      val sm = new SendMsgDialog(null, apiHandlers.sender, None, None)
      sm.visible = true
    }
  }
  val clearAction = new Action("Clear") {
    toolTip = "Removes all tweets (including filtered-out ones)"
    def apply = clearTweets
  }
  val clearRepliesAction = new Action("Clear") {
    toolTip = "Removes all mentions"
    def apply = clearTweets
  }
  var detailsButton: JToggleButton = _ 
  val showDetailsAction = new Action("Details") {
    toolTip = "Shows or hides the details panel"
    def apply = {
      tweetDetailPanel.visible = detailsButton.isSelected    
    }
  }
  detailsButton = new JToggleButton(showDetailsAction.peer)
  detailsButton.setSelected(true)

  var geoButton: JToggleButton = _ 
  val geoAction = new Action("Geo") {
    toolTip = "Enables lookup of locations from latitude and longitude"
    def apply = {
      tweetDetailPanel.geoEnabled = geoButton.isSelected    
    }
  }
  geoButton = new JToggleButton(geoAction.peer)
  geoButton.setSelected(true)

  var animButton: JToggleButton = _ 
  val animAction = new Action("Anim") {
    toolTip = "Enables simple, useful animations"
    def apply = {
      tweetDetailPanel.enableAnimation(animButton.isSelected)    
    }
  }
  animButton = new JToggleButton(animAction.peer)
  animButton.setSelected(true)

  var dockedButton: JToggleButton = _ 
  val dockedAction = new Action("Docked") {
    toolTip = "Docks or frees the pane"
    def apply = {
      if (! dockedButton.isSelected) {
        Windows.undock(StatusPane.this)
      } else {
        Windows.dock(StatusPane.this)
      }
    }
  }
  dockedButton = new JToggleButton(dockedAction.peer)
  dockedButton.setSelected(true)

  statusTableModel.addTableModelListener(this)
  statusTableModel.setPreChangeListener(this)
  
  def toolbar: JToolBar = null

  if (toolbar != null)
    peer.add(toolbar, new Constraints{grid=(0,0); gridwidth=3}.peer)
  
  add(new ScrollPane {
    table = newTable
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  val tweetDetailPanel = new TweetDetailPanel(table, filtersDialog, streams)
  add(tweetDetailPanel, new Constraints{
    grid = (0,3); fill = GridBagPanel.Fill.Horizontal;
  })
  
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
  
  def newTable: StatusTable = new StatusTable(statusTableModel, apiHandlers, clearAction, showBigPicture)
  
  def showBigPicture = tweetDetailPanel.showBigPicture
  
  def tableChanging = {
    if (table != null) {
      lastSelectedRows = table.getSelectedStatuses
    }
  }

  def tableChanged(e: TableModelEvent) = {
    if (table != null && e.getFirstRow != e.getLastRow) {
      val selectionModel = table.getSelectionModel
      selectionModel.clearSelection
      
      for (i <- 0 until table.getRowCount) {
        if (lastSelectedRows.contains(statusTableModel.getStatusAt(table.convertRowIndexToModel(i)))) {
          selectionModel.addSelectionInterval(i, i)
        }
      }
    }
  }
  
  def clearTweets {
    clearSelection
    statusTableModel.clear
    tweetDetailPanel.clearStatusDetails
  }
  
  def clearSelection {
    table.getSelectionModel.clearSelection
    lastSelectedRows = Nil
  }

  def requestFocusForTable {
    table.requestFocusInWindow
  }
}

class TweetsStatusPane(title: String, statusTableModel: StatusTableModel, apiHandlers: ApiHandlers, 
    filterSet: FilterSet, streams: Streams) 
    extends StatusPane(title, statusTableModel, apiHandlers, filterSet, streams) {
  
  override def toolbar: JToolBar = new JToolBar {
    setFloatable(false)
    add(sendAction.peer)
    add(showFiltersAction.peer)
    add(clearAction.peer)
    addSeparator
    add(dockedButton)
    add(detailsButton)
    add(geoButton)
    add(animButton)
  }

  override def newTable: StatusTable = new TweetsTable(statusTableModel, apiHandlers, clearAction, showBigPicture)
  
}

class RepliesStatusPane(title: String, statusTableModel: StatusTableModel, apiHandlers: ApiHandlers, 
    filterSet: FilterSet, streams: Streams) 
    extends StatusPane(title, statusTableModel, apiHandlers, filterSet, streams) {
  
  override def toolbar: JToolBar = new JToolBar {
    setFloatable(false)
    add(sendAction.peer)
    add(showFiltersAction.peer)
    add(clearRepliesAction.peer)
    addSeparator
    add(dockedButton)
    add(detailsButton)
    add(geoButton)
    add(animButton)
  }

}
