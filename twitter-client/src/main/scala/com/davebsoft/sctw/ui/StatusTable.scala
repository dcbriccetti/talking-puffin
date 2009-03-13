package com.davebsoft.sctw.ui
import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import java.awt.{Desktop}
import java.awt.event.{KeyEvent, ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.net.{URI, URL}
import java.util.Comparator
import javax.swing.{JTable, JMenu, JMenuItem, JPopupMenu, KeyStroke}
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Table of statuses.
 * @author Dave Briccetti
 */

class StatusTable(statusTableModel: StatusTableModel, statusSelected: (NodeSeq) => Unit) 
    extends JTable(statusTableModel) {
  setRowSorter(new TableRowSorter[StatusTableModel](statusTableModel))
  
  val colModel = getColumnModel
  
  val ageCol = colModel.getColumn(0)
  ageCol.setPreferredWidth(60)
  ageCol.setMaxWidth(100)
  ageCol.setCellRenderer(new AgeCellRenderer)
  
  val nameCol = colModel.getColumn(1)
  nameCol.setPreferredWidth(100)
  nameCol.setMaxWidth(200)
  
  val statusCol = colModel.getColumn(2)
  statusCol.setPreferredWidth(600)

  val muteAction = Action("Mute") {statusTableModel.muteSelectedUsers(getSelectedModelIndexes)}
  getActionMap.put(muteAction.title, muteAction.peer)
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), muteAction.title)
  
  val nextAction = Action("Next") {
    dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
      0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED))
  }
  getActionMap.put(nextAction.title, nextAction.peer)
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), nextAction.title)
  
  val prevAction = Action("Previous") {
    dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
      0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED))
  }
  getActionMap.put(prevAction.title, prevAction.peer)
  getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), prevAction.title)
  
  addMouseListener(new PopupListener(this, getPopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      if (e.getClickCount == 2) {
        val status = statusTableModel.getStatusAt(convertRowIndexToModel(getSelectedRow))
        var uri = "http://twitter.com/" +
          (status \ "user" \ "screen_name").text + "/statuses/" + (status \ "id").text
        if (Desktop.isDesktopSupported) {
          Desktop.getDesktop.browse(new URI(uri))
        }
      }
    }
  })
  
  getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(e: ListSelectionEvent) = {
      if (! e.getValueIsAdjusting) {
        if (getSelectedRowCount == 1) {
          showDetailsForTableRow(getSelectedRow)
        }
      }
    }
  })

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu()

    val mi = new MenuItem(muteAction)
    menu.add(mi.peer)

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
    val tableRows = getSelectedRows
    var smi = List[Int]()
    for (i <- 0 to (tableRows.length - 1)) {
      smi ::= convertRowIndexToModel(tableRows(i))
    }
    smi
  }
  
  private def showDetailsForTableRow(r: Int) {
    try {
      val modelRowIndex = convertRowIndexToModel(r)
      val status = statusTableModel.getStatusAt(modelRowIndex)
      statusSelected(status)
    } catch {
      case ex: IndexOutOfBoundsException => println(ex)
    }
  }
  
}