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
  private var lastSelectedRows: List[NodeSeq] = Nil
  private val filtersDialog = new FiltersDialog(title, statusTableModel, filterSet)

  statusTableModel.addTableModelListener(this)
  statusTableModel.setPreChangeListener(this)
  
  val statusToolBar = new StatusToolBar(filtersDialog, apiHandlers, this, clearTweets)
  peer.add(statusToolBar, new Constraints{grid=(0,0); gridwidth=3}.peer)
  
  add(new ScrollPane {
    table = newTable
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  private val tweetDetailPanel = new TweetDetailPanel(table, filtersDialog, streams)
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
  
  def newTable: StatusTable = new StatusTable(statusTableModel, apiHandlers, 
    statusToolBar.clearAction, showBigPicture)
  
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

  def requestFocusForTable = table.requestFocusInWindow
}

