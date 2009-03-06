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
import javax.swing._
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Displays friend statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends GridBagPanel 
        with TableModelListener with PreChangeListener {
  var table: JTable = _
  var showingUrl: String = _
  private val THUMBNAIL_SIZE = 48
  val transparentPic = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB))
  var picLabel: Label = _
  var userDescription: TextArea = _
  var largeTweet: JTextPane = _
  val emptyIntArray = new Array[Int](0) 
  var lastSelectedRows = emptyIntArray 

  statusTableModel.addTableModelListener(this)
  statusTableModel.setPreChangeListener(this)
  
  add(new ScrollPane {
    table = buildTable    
    peer.setViewportView(table)
  }, new Constraints{
    gridx = 0; gridy = 0; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  largeTweet = new JTextPane()
  val dim = new Dimension(500, 70)
  largeTweet.setMinimumSize(dim)
  largeTweet.setPreferredSize(dim)
  largeTweet.setBackground(StatusPane.this.background)
  largeTweet.setContentType("text/html");
  largeTweet.setEditable(false);
  largeTweet.addHyperlinkListener(new HyperlinkListener() {
    def hyperlinkUpdate(e: HyperlinkEvent) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        if (Desktop.isDesktopSupported) {
          Desktop.getDesktop.browse(e.getURL().toURI)
          table.requestFocusInWindow // Let user resume using keyboard to move through tweets
        }
      }
    }
  });
  
  
  peer.add(largeTweet, new Constraints{
    insets = new Insets(5,1,5,1)
    gridx = 0; gridy = 1; fill = GridBagPanel.Fill.Both;
  }.peer)

  add(new ControlPanel, new Constraints{
    gridx = 0; gridy = 2; fill = GridBagPanel.Fill.Horizontal;
  })

  def tableChanging = lastSelectedRows = table.getSelectedRows

  def tableChanged(e: TableModelEvent) = {
    e match {
      case _ =>
        val selectionModel = table.getSelectionModel
        selectionModel.clearSelection
        
        for (i <- 0 until lastSelectedRows.length) {
          val row = lastSelectedRows(i)
          selectionModel.addSelectionInterval(row, row)
        }
    }
  }

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu()

    val mi = new JMenuItem("Mute")
    mi.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.muteSelectedUsers(getSelectedModelIndexes)
      }
    })
    menu.add(mi)

    val tagAl = new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.tagSelectedUsers(getSelectedModelIndexes, e.getActionCommand)
      }
    }
    
    val tagMi = new JMenu("Tag Friend With")
    for (tag <- TagsRepository.get) {
      val tagSmi = new JMenuItem(tag)
      tagSmi.addActionListener(tagAl)
      tagMi.add(tagSmi)
    }
    menu.add(tagMi)

    menu
  }

  private def getSelectedModelIndexes: List[Int] = {
    val tableRows = table.getSelectedRows
    var smi = List[Int]()
    for (i <- 0 to (tableRows.length - 1)) {
      smi ::= table.convertRowIndexToModel(tableRows(i))
    }
    smi
  }
  
  private def buildTable: JTable = {
    val table = new JTable(statusTableModel)
    val sorter = new TableRowSorter[StatusTableModel](statusTableModel);
    table.setRowSorter(sorter);
    
    val colModel = table.getColumnModel
    
    val ageCol = colModel.getColumn(0)
    ageCol.setPreferredWidth(60)
    ageCol.setMaxWidth(100)
    ageCol.setCellRenderer(new AgeCellRenderer);
    
    val nameCol = colModel.getColumn(1)
    nameCol.setPreferredWidth(100)
    nameCol.setMaxWidth(200)
    
    val statusCol = colModel.getColumn(2)
    statusCol.setPreferredWidth(600)
    statusCol.setCellRenderer(new StatusCellRenderer);

    table.addMouseListener(new PopupListener(table, getPopupMenu));
    table.addMouseMotionListener(new MouseAdapter {
      override def mouseMoved(e: MouseEvent) = {
        sendEventToRenderer(e)
      }
    })
    table.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = sendEventToRenderer(e)
      override def mousePressed(e: MouseEvent) = sendEventToRenderer(e)
      override def mouseReleased(e: MouseEvent) = sendEventToRenderer(e)
    })
    table.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        if (e.getClickCount == 2) {
          val status = statusTableModel.getStatusAt(table.convertRowIndexToModel(
            table.getSelectedRow))
          var uri = "http://twitter.com/" +
                  (status \ "user" \ "screen_name").text + "/statuses/" +
                  (status \ "id").text
          if (Desktop.isDesktopSupported) {
            Desktop.getDesktop.browse(new URI(uri))
          }
        }
      }
    })
    
    table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent) = {
        if (! e.getValueIsAdjusting) {
          if (table.getSelectedRowCount == 1) {
            showDetailsForTableRow(table.getSelectedRow)
          }
        }
      }
    })

    table
  }
  
  private def sendEventToRenderer(e: MouseEvent) {
    val c = table.columnAtPoint(e.getPoint)
    val r = table.rowAtPoint(e.getPoint)
    if (c != -1 && r != -1) {
      val renderer = table.getCellRenderer(r, c)
      renderer match {
        case fcr: StatusCellFancyRenderer => {
          println("Renderer at " + e.getPoint + ": " + renderer)
          fcr.text.dispatchEvent(SwingUtilities.convertMouseEvent(table, e, fcr.text))
        }
        case _ =>
      }
    }
  }

  private def showDetailsForTableRow(r: Int) {
    try {
      val modelRowIndex = table.convertRowIndexToModel(r)
      val status = statusTableModel.getStatusAt(modelRowIndex)
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
              println("got " + picUrl)
            }
          }
        }.execute
      }
    } catch {
      case ex: IndexOutOfBoundsException => println(ex)
    }
  }
  
  private class ControlPanel extends GridBagPanel {
    
    private class CustomConstraints extends Constraints {
      gridy = 0; anchor = Anchor.SouthWest; insets = new Insets(0, 4, 0, 0)
    }
  
    picLabel = new Label
    picLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) = {
        val picture = new Label {
          icon = new ImageIcon(new URL(showingUrl.replace("_normal", "")))
        }
        val bigPicFrame = new Frame {
          contents = picture
          pack
          peer.setLocationRelativeTo(picLabel.peer)
          visible = true
        }
        picture.peer.addMouseListener(new MouseAdapter {
          override def mouseClicked(e: MouseEvent) = {
            bigPicFrame.dispose
          }
        })
      }
    })
    add(picLabel, new CustomConstraints {
      gridx = 0; gridy = 0; gridheight = 2;  
    })

    userDescription = new TextArea {
      background = ControlPanel.this.background
      lineWrap = true
      wordWrap = true
      editable = false
    }
    add(userDescription, new CustomConstraints {
      gridx = 1; gridy = 0; gridheight=2; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1;
    })

    add(new Label("Refresh (secs)"), new CustomConstraints { gridx = 2; gridy = 1; anchor=Anchor.CENTER })
    val comboBox = new ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60))
    var defaultRefresh = 120
    comboBox.peer.setSelectedItem(defaultRefresh)
    statusTableModel.setUpdateFrequency(defaultRefresh)
    comboBox.peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        statusTableModel.setUpdateFrequency(comboBox.selection.item)
      }
    })
    add(comboBox, new CustomConstraints { gridx=3; gridy=1; anchor=Anchor.CENTER })
    
    val lastSetButton = new Button("Last 200") {
      tooltip = "Loads the last 200 of your “following” tweets"
    }
    listenTo(lastSetButton)
    add(lastSetButton, new CustomConstraints { gridx=4; gridy=1; anchor=Anchor.CENTER })
    
    val clearButton = new Button("Clear")
    listenTo(clearButton)
    add(clearButton, new CustomConstraints { gridx=5; gridy=1; anchor=Anchor.CENTER })
    
    reactions += {
      case ButtonClicked(b) => {
        if (b == clearButton) {
          clearSelection
          statusTableModel.clear
          picLabel.icon = null
          userDescription.text = null
          largeTweet.setText(null)
        } else if (b == lastSetButton) {
          clearSelection
          statusTableModel.loadLastSet
        }
      }
    }
    
    def clearSelection = {
      table.getSelectionModel.clearSelection
      lastSelectedRows = emptyIntArray
    }
  }
}
