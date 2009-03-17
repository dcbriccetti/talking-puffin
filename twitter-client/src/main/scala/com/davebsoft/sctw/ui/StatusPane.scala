package com.davebsoft.sctw.ui

import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.image.BufferedImage
import java.awt.{Color, Desktop, Dimension, Insets, Font}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.{JTable, JTextPane, JButton, JLabel, ImageIcon, Icon, SwingWorker, JMenu, JPopupMenu, JMenuItem, JToolBar}
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Displays friend statuses
 */
class StatusPane(statusTableModel: StatusTableModel, filtersPane: FiltersPane) extends GridBagPanel 
        with TableModelListener with PreChangeListener {
  var table: JTable = _
  var showingUrl: String = _
  private val THUMBNAIL_SIZE = 48
  val transparentPic = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
  var picLabel: Label = _
  var bigPicFrame: Frame = _
  var bigPicLabel: Label = _
  var userDescription: TextArea = _
  var largeTweet: JTextPane = _
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
    table = new StatusTable(statusTableModel, showStatusDetails, clearAction, showBigPicture)
    peer.setViewportView(table)
  }, new Constraints{
    grid = (0,1); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  largeTweet = new LargeTweet(filtersPane, table)
  largeTweet.setBackground(StatusPane.this.background)
  
  peer.add(largeTweet, new Constraints{
    insets = new Insets(5,1,5,1)
    grid = (0,2); fill = GridBagPanel.Fill.Both;
  }.peer)

  add(new ControlPanel, new Constraints{
    grid = (0,3); fill = GridBagPanel.Fill.Horizontal;
  })

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
    picLabel.icon = null
    userDescription.text = null
    largeTweet.setText(null)
  }

  def clearSelection {
    table.getSelectionModel.clearSelection
    lastSelectedRows = emptyIntArray
  }

  private def showStatusDetails(status: NodeSeq) {
    val user = status \ "user"
    userDescription.text = (user \ "name").text + " • " +
            (user \ "location").text + " • " + (user \ "description").text
    largeTweet.setText(HtmlFormatter.createTweetHtml((status \ "text").text, 
      (status \ "in_reply_to_status_id").text)) 
    val picUrl = (user \ "profile_image_url").text
    if (! picUrl.equals(showingUrl)) {
      showingUrl = picUrl
      val u = new URL(picUrl)
      picLabel.icon = transparentPic
      new SwingWorker[Icon, Object] {
        val urlToShow = showingUrl
        def doInBackground = new ImageIcon(u)
        override def done = {
          if (urlToShow == showingUrl) { // If user is moving quickly there may be several threads
            val icon = get
            if (icon.getIconHeight <= THUMBNAIL_SIZE) picLabel.icon = icon // Ignore broken, too-big thumbnails 
            setBigPicLabelIcon
          }
        }
      }.execute
    }
  }

  private def setBigPicLabelIcon {
    if (bigPicFrame != null && bigPicLabel != null) { 
      bigPicLabel.icon = new ImageIcon(new URL(showingUrl.replace("_normal", "")))
      bigPicFrame.pack
    }
  }

  def showBigPicture {
    bigPicLabel = new Label
    if (bigPicFrame != null) {
      bigPicFrame.dispose
    }
    bigPicFrame = new Frame {
      contents = bigPicLabel
      peer.setLocationRelativeTo(picLabel.peer)
      visible = true
    }
    setBigPicLabelIcon
    bigPicLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        bigPicFrame.dispose
        bigPicFrame = null
        bigPicLabel = null
      }
    })
  }
  
  private class ControlPanel extends GridBagPanel {
    
    private class CustomConstraints extends Constraints {
      gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
    }
  
    picLabel = new Label
    picLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        showBigPicture
      }
    })
    add(picLabel, new CustomConstraints {
      grid = (0,0); gridheight = 2;  
    })

    userDescription = new TextArea {
      background = ControlPanel.this.background
      lineWrap = true
      wordWrap = true
      editable = false
    }
    add(userDescription, new CustomConstraints {
      grid = (1,0); gridheight=2; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1;
    })

  }
}
