package com.davebsoft.sctw.ui

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
 * Displays friend and public statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends GridBagPanel 
        with TableModelListener with PreChangeListener {
  var table: JTable = _
  var unmuteButton: Button = _
  var showingUrl: String = _
  val transparentPic = new ImageIcon(new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB))
  var picLabel: Label = _
  var userDescription: TextArea = _
  var largeTweet: JTextPane = _
  var lastSelectedRows = new Array[Int](0);

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
        unmuteButton.enabled = true
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
              picLabel.icon = get
              println("got " + picUrl)
            }
          }
        }.execute
      }
    } catch {
      case ex: IndexOutOfBoundsException => println(ex)
    }
  }
  
  private class ControlPanel extends FlowPanel(FlowPanel.Alignment.Left) {
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
    contents += picLabel

    userDescription = new TextArea {
      background = ControlPanel.this.background
      columns = 25
      lineWrap = true
      wordWrap = true
      editable = false
    }
    contents += userDescription

    contents += new Label("Refresh (secs)")
    val comboBox = new ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60))
    var defaultRefresh = 120
    comboBox.peer.setSelectedItem(defaultRefresh)
    statusTableModel.setUpdateFrequency(defaultRefresh)
    comboBox.peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        statusTableModel.setUpdateFrequency(comboBox.selection.item)
      }
    });
    contents += comboBox
    
    val lastSetButton = new Button("Last 200") {
      tooltip = "Loads the last 200 of your “following” tweets"
    }
    listenTo(lastSetButton)
    contents += lastSetButton
    
    val clearButton = new Button("Clear")
    listenTo(clearButton)
    contents += clearButton
    
    unmuteButton = new Button("Unmute All")
    listenTo(unmuteButton)
    unmuteButton.enabled = false
    contents += unmuteButton

    reactions += {
      case ButtonClicked(b) => {
        if (b == clearButton) {
          statusTableModel.clear
          picLabel.icon = null
          userDescription.text = null
          largeTweet.setText(null)
        } else if (b == unmuteButton) { 
          statusTableModel.unMuteAll
          unmuteButton.enabled = false
        } else if (b == lastSetButton) { 
          statusTableModel.loadLastSet
        }
      }
    }
  }
}
