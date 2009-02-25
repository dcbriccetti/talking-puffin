package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.event.{MouseEvent, ActionEvent, MouseAdapter, ActionListener}
import java.awt.{Desktop, Dimension}
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Displays friend and public statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends GridBagPanel {
  var table: JTable = null
  var unmuteButton: Button = null
  var showingUrl: String = null
  var picLabel: Label = null
  
  add(new ScrollPane {
    table = buildTable    
    peer.setViewportView(table)
  }, new Constraints{
    gridx = 0; gridy = 0; fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; 
  })
  
  add(new FlowPanel {
    picLabel = new Label
    contents += picLabel

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
    lastSetButton.peer.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.loadLastSet
      }
    })
    contents += lastSetButton
    
    val clearButton = new Button("Clear")
    clearButton.peer.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.clear
      }
    })
    contents += clearButton
    
    unmuteButton = new Button("Unmute All")
    unmuteButton.peer.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.unMuteAll
        unmuteButton.enabled = false
      }
    })
    unmuteButton.enabled = false
    contents += unmuteButton
  }, new Constraints{
    gridx = 0; gridy = 1; fill = GridBagPanel.Fill.Horizontal;
  })

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
        doCustomTooltip(e)
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

  private def doCustomTooltip(e: MouseEvent) {
    val c = table.columnAtPoint(e.getPoint)
    val r = table.rowAtPoint(e.getPoint)
    if (c == 1 && r != -1) {
      val user = statusTableModel.getValueAt(
        table.convertRowIndexToModel(r), 1).asInstanceOf[NodeSeq]
      val picUrl = (user \ "profile_image_url").text
      if (! picUrl.equals(showingUrl)) {
        showingUrl = picUrl
        val u = new URL(picUrl)
        val icon = new ImageIcon(u)
        picLabel.peer.setIcon(icon)
        println("got " + picUrl)
      }
    }
  }
}
