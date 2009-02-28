package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.{Desktop, Dimension, Insets, Font}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing._
import javax.swing.event.{TableModelEvent, ListSelectionEvent, TableModelListener, ListSelectionListener}
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Displays friend and public statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends GridBagPanel 
        with TableModelListener with PreChangeListener {
  var table: JTable = null
  var unmuteButton: Button = null
  var showingUrl: String = null
  var picLabel: Label = null
  var userDescription: TextArea = null
  var largeTweet: TextArea = null
  var lastSelectedRows = new Array[Int](0);

  statusTableModel.addTableModelListener(this)
  statusTableModel.setPreChangeListener(this)
  
  add(new ScrollPane {
    table = buildTable    
    peer.setViewportView(table)
  }, new Constraints{
    gridx = 0; gridy = 0; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  largeTweet = new TextArea {
    val dim = new Dimension(500, 50)
    minimumSize = dim
    preferredSize = dim
    background = StatusPane.this.background
    font = new Font("Serif", Font.PLAIN, 24)
    lineWrap = true
    wordWrap = true
  }
  add(largeTweet, new Constraints{
    insets = new Insets(5,1,5,1)
    gridx = 0; gridy = 1; fill = GridBagPanel.Fill.Both;
  })

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
    sorter.setComparator(1, new Comparator[NodeSeq] {
      def compare(o1: NodeSeq, o2: NodeSeq) = 
        (o1 \ "name").text compareTo (o2 \ "name").text
    });
    table.setRowSorter(sorter);
    
    val colModel = table.getColumnModel
    
    val ageCol = colModel.getColumn(0)
    ageCol.setPreferredWidth(60)
    ageCol.setMaxWidth(100)
    ageCol.setCellRenderer(new AgeCellRenderer);
    
    val nameCol = colModel.getColumn(1)
    nameCol.setPreferredWidth(100)
    nameCol.setMaxWidth(200)
    nameCol.setCellRenderer(new NameCellRenderer);
    
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
      val picUrl = (user \ "profile_image_url").text
      if (! picUrl.equals(showingUrl)) {
        showingUrl = picUrl
        val u = new URL(picUrl)
        val icon = new ImageIcon(u)
        picLabel.icon = icon
        println("got " + picUrl)
      }
      userDescription.text = (user \ "screen_name").text + " • " +
              (user \ "location").text + " • " + (user \ "description").text
      largeTweet.text = (status \ "text").text
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
          largeTweet.text = null
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
