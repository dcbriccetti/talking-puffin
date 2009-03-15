package com.davebsoft.sctw.ui

import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.xml.{NodeSeq, Node}

import _root_.scala.{Option}
import java.awt.{Desktop}
import java.awt.event.{KeyEvent, ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.net.{URI, URL}
import java.util.Comparator
import java.util.regex.Pattern
import javax.swing.{JTable, JMenu, JMenuItem, JPopupMenu, KeyStroke}
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import scala.swing._
import filter.TagsRepository

/**
 * Table of statuses.
 * @author Dave Briccetti
 */

class StatusTable(statusTableModel: StatusTableModel, statusSelected: (NodeSeq) => Unit,
      clearAction: Action) 
    extends JTable(statusTableModel) {
  setRowSorter(new TableRowSorter[StatusTableModel](statusTableModel))
  
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer with ZebraStriping)
  
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

  val viewAction = Action("View in Browser") {viewSelected}
  viewAction.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0)) 
  getActionMap.put(viewAction.title, viewAction.peer)
  viewAction.accelerator match {
    case Some(s) => getInputMap.put(s, viewAction.title)
    case None =>
  }
  
  val openAction = Action("Open Links") {
    val status = getSelectedStatus
    val urls = LinkExtractor.getAllLinks(status)
    
    if (urls.length == 1) {
      browse(urls(0))
    } else if (urls.length > 1) {
      val menu = new JPopupMenu
      var index = 0
      
      for (url <- urls) {
        val a1 = Action(url) {browse(url)}
        a1.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_1 + index, 0)) 
        index += 1
        menu.add(new MenuItem(a1).peer)
      }
      val menuLoc = this.getCellRect(getSelectedRow, 0, true).getLocation
      menu.show(this, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
    }
  }
  openAction.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0))
  connectAction(openAction, KeyStroke.getKeyStroke(KeyEvent.VK_L, 0))
  
  val muteAction = Action("Mute") {statusTableModel.muteSelectedUsers(getSelectedModelIndexes)}
  muteAction.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0))
  connectAction(muteAction, KeyStroke.getKeyStroke(KeyEvent.VK_M, 0))
  
  connectAction(clearAction, KeyStroke.getKeyStroke(KeyEvent.VK_C, 0))
  
  val nextAction = Action("Next") {
    dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
      0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED))
  }
  connectAction(nextAction, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0))
  
  val prevAction = Action("Previous") {
    dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
      0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED))
  }
  connectAction(prevAction, KeyStroke.getKeyStroke(KeyEvent.VK_P, 0))
  
  addMouseListener(new PopupListener(this, getPopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      if (e.getClickCount == 2) {
        viewSelected
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
  
  private def connectAction(a: Action, k: KeyStroke) {
    getActionMap.put(a.title, a.peer)
    getInputMap.put(k, a.title)
  }

  def viewSelected {
    val status = getSelectedStatus
    var uri = "http://twitter.com/" +
      (status \ "user" \ "screen_name").text + "/statuses/" + (status \ "id").text
    browse(uri)
  }
  
  def browse(uri: String) {
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(uri))
    }
  }

  def getSelectedStatus: NodeSeq = {
    statusTableModel.getStatusAt(convertRowIndexToModel(getSelectedRow))
  }

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu

    menu.add(new MenuItem(viewAction).peer)
    menu.add(new MenuItem(openAction).peer)
    menu.add(new MenuItem(muteAction).peer)

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