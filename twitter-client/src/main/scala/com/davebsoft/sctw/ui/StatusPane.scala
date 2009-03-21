package com.davebsoft.sctw.ui

import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.awt.event.{KeyEvent, KeyAdapter}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.{JTable, JTextPane, JButton, JLabel, ImageIcon, Icon, SwingWorker, JMenu, JPopupMenu, JMenuItem, JToolBar}
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository
import twitter.Sender

/**
 * Displays friend statuses
 */
class StatusPane(statusTableModel: StatusTableModel, sender: Sender, filtersPane: FiltersPane) 
    extends GridBagPanel with TableModelListener with PreChangeListener {
  var table: JTable = _
  val emptyIntArray = new Array[Int](0) 
  var lastSelectedRows = emptyIntArray
  val clearAction = new Action("Clear") {
    toolTip = "Removes all tweets from the display"
    def apply = clearTweets
  }
  val last200Action = new Action("Fetch") {
    toolTip = "Fetches the last 200 of your “following” tweets"
    def apply = {
      clearSelection
      statusTableModel.loadLastSet
    }
  }

  statusTableModel.addTableModelListener(this)
  statusTableModel.setPreChangeListener(this)
  
  peer.add(new JToolBar {
    setFloatable(false)
    add(clearAction.peer)
    add(last200Action.peer)
    val comboBox = new ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60))
    comboBox.peer.setToolTipText("Number of seconds between fetches from Twitter")
    var defaultRefresh = 120
    comboBox.peer.setSelectedItem(defaultRefresh)
    statusTableModel.setUpdateFrequency(defaultRefresh)
    comboBox.peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        statusTableModel.setUpdateFrequency(comboBox.selection.item)
      }
    })
    add(comboBox.peer)
  }, new Constraints{grid=(0,0); gridwidth=3}.peer)
  
  add(new ScrollPane {
    table = new StatusTable(statusTableModel, sender, clearAction, showBigPicture)
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  val tweetDetailPanel = new TweetDetailPanel(table, filtersPane)
  add(tweetDetailPanel, new Constraints{
    grid = (0,3); fill = GridBagPanel.Fill.Horizontal;
  })

  def showBigPicture = tweetDetailPanel.showBigPicture
  
  def tableChanging = if (table != null) lastSelectedRows = table.getSelectedRows

  def tableChanged(e: TableModelEvent) = {
    if (table != null) {
      val selectionModel = table.getSelectionModel
      selectionModel.clearSelection
      
      for (i <- 0 until lastSelectedRows.length) {
        val row = lastSelectedRows(i)
        selectionModel.addSelectionInterval(row, row)
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
    lastSelectedRows = emptyIntArray
  }

}

