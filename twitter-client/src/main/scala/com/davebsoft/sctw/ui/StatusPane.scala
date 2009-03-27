package com.davebsoft.sctw.ui

import _root_.scala.swing.event.{ComponentResized, ButtonClicked}
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.awt.event.{KeyEvent, KeyAdapter}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.{JTable, JTextPane, JButton, JToggleButton, JLabel, ImageIcon, Icon, SwingWorker, JMenu, JPopupMenu, JMenuItem, JToolBar}
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
  var table: StatusTable = _
  var lastSelectedRows: List[NodeSeq] = Nil
  val sendAction = new Action("Send…") {
    toolTip = "Opens a window from which you can send a tweet"
    def apply { 
      val sm = new SendMsgDialog(null, sender, None)
      sm.visible = true
    }
  }
  val clearAction = new Action("Clear") {
    toolTip = "Removes all tweets from the display"
    def apply = clearTweets
  }
  val loadNewAction = new Action("Load New") {
    toolTip = "Loads any new tweets since the last"
    def apply = {
      statusTableModel.loadNewData
    }
  }
  val last200Action = new Action("Last 200") {
    toolTip = "Fetches the last 200 tweets"
    def apply = {
      clearSelection
      statusTableModel.loadLastSet
    }
  }
  var detailsButton: JToggleButton = _ 
  val showDetailsAction = new Action("Details") {
    toolTip = "Hides or shows the details panel"
    def apply = {
      tweetDetailPanel.visible = detailsButton.isSelected    
    }
  }
  detailsButton = new JToggleButton(showDetailsAction.peer)
  detailsButton.setSelected(true)

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
  
  val tweetDetailPanel = new TweetDetailPanel(table, filtersPane)
  add(tweetDetailPanel, new Constraints{
    grid = (0,3); fill = GridBagPanel.Fill.Horizontal;
  })
  
  statusTableModel.loadNewData

  def newTable: StatusTable = new StatusTable(statusTableModel, sender, clearAction, showBigPicture)
  
  def showBigPicture = tweetDetailPanel.showBigPicture
  
  def tableChanging = {
    if (table != null) {
      lastSelectedRows = table.getSelectedModelIndexes map(statusTableModel.getStatusAt(_))
    }
  }

  def tableChanged(e: TableModelEvent) = {
    if (table != null) {
      val selectionModel = table.getSelectionModel
      selectionModel.clearSelection
      
      for (i <- 0 until table.getRowCount) {
        if (lastSelectedRows.contains(statusTableModel.getStatusAt(table.convertRowIndexToModel(i)))) {
          println("selecting row " + i)
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

}

class ToolbarStatusPane(statusTableModel: StatusTableModel, sender: Sender, filtersPane: FiltersPane) 
    extends StatusPane(statusTableModel, sender, filtersPane) {
  
  override def toolbar: JToolBar = new JToolBar {
    setFloatable(false)
    add(sendAction.peer)
    add(clearAction.peer)
    add(loadNewAction.peer)
    add(last200Action.peer)
    val comboBox = new ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60))
    comboBox.peer.setToolTipText("Number of seconds between automatic “Load New”s")
    var defaultRefresh = 120
    comboBox.peer.setSelectedItem(defaultRefresh)
    statusTableModel.setUpdateFrequency(defaultRefresh)
    comboBox.peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        statusTableModel.setUpdateFrequency(comboBox.selection.item)
      }
    })
    add(comboBox.peer)
    add(detailsButton)
  }

  override def newTable: StatusTable = new TweetsTable(statusTableModel, sender, clearAction, showBigPicture)
  
}