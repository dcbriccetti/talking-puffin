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
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import javax.swing.{JTable, KeyStroke, JMenuItem, JMenu, JPopupMenu}

import scala.swing._
import filter.TagsRepository
import twitter.Sender

/**
 * Table of statuses.
 * @author Dave Briccetti
 */

class StatusTable(statusTableModel: StatusTableModel, sender: Sender, statusSelected: (NodeSeq) => Unit,
      clearAction: Action, showBigPicture: => Unit) 
    extends JTable(statusTableModel) {
  val sorter = new TableRowSorter[StatusTableModel](statusTableModel)
  sorter.setComparator(1, new Comparator[AnnotatedUser] {
    def compare(o1: AnnotatedUser, o2: AnnotatedUser) = o1.name.compareToIgnoreCase(o2.name)
  })
  setRowSorter(sorter)
  
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer with ZebraStriping)
  
  val colModel = getColumnModel
  
  val ageCol = colModel.getColumn(0)
  ageCol.setPreferredWidth(60)
  ageCol.setMaxWidth(100)
  ageCol.setCellRenderer(new AgeCellRenderer)
  
  val nameCol = colModel.getColumn(1)
  nameCol.setPreferredWidth(100)
  nameCol.setMaxWidth(200)
  nameCol.setCellRenderer(new AnnotatedUserRenderer with ZebraStriping)
  
  val statusCol = colModel.getColumn(2)
  statusCol.setPreferredWidth(600)

  val actions = buildActions

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
  
  def viewSelected {
    getSelectedStatus match {
      case Some(status) =>
        var uri = "http://twitter.com/" +
                (status \ "user" \ "screen_name").text + "/statuses/" + (status \ "id").text
        browse(uri)
      case None =>
    }
  }
  
  def browse(uri: String) {
    if (Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(uri))
    }
  }
  
  def reply {
    val sm = new SendMsgDialog(null, sender, getSelectedStatus)
    sm.visible = true
  }

  def getSelectedStatus: Option[NodeSeq] = {
    val row = getSelectedRow
    if (row == -1) None else Some(statusTableModel.getStatusAt(convertRowIndexToModel(row)))
  }

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu

    for (action <- actions.reverse) 
      menu.add(new MenuItem(action).peer)

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

  private def buildActions: List[Action] = {
    var actions = List[Action]()
    val viewAction = Action("View in Browser") {viewSelected}
    viewAction.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0)) 
    getActionMap.put(viewAction.title, viewAction.peer)
    viewAction.accelerator match {
      case Some(s) => getInputMap.put(s, viewAction.title)
      case None =>
    }
    actions ::= viewAction
  
    getSelectedStatus match {
      case Some(status) => {
        val openLinksAction = new OpenLinksAction(status, this, browse)
        val l = KeyStroke.getKeyStroke(KeyEvent.VK_L, 0)
        openLinksAction.accelerator = Some(l)
        connectAction(openLinksAction, l)
        actions ::= openLinksAction
      }
      case None =>
    }
  
    val muteAction = Action("Mute") {statusTableModel.muteSelectedUsers(getSelectedModelIndexes)}
    muteAction.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0))
    connectAction(muteAction, KeyStroke.getKeyStroke(KeyEvent.VK_M, 0))
    actions ::= muteAction
  
    val c = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)
    clearAction.accelerator = Some(c)
    connectAction(clearAction, c)
    actions ::= clearAction
  
    val nextAction = Action("Next") {
      dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
        0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED))
    }
    val n = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0)
    nextAction.accelerator = Some(n)
    connectAction(nextAction, n)
    actions ::= nextAction
  
    val prevAction = Action("Previous") {
      dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
        0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED))
    }
    val p = KeyStroke.getKeyStroke(KeyEvent.VK_P, 0)
    prevAction.accelerator = Some(p)
    connectAction(prevAction, p)
    actions ::= prevAction
  
    val showImageAction = Action("Show Larger Image") { showBigPicture }
    val i = KeyStroke.getKeyStroke(KeyEvent.VK_I, 0)
    showImageAction.accelerator = Some(i)
    connectAction(showImageAction, i)
    actions ::= showImageAction
  
    val replyAction = Action("Reply") { reply }
    val r = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)
    replyAction.accelerator = Some(r)
    connectAction(replyAction, r)
    actions ::= replyAction
  
    val deleteAction = Action("Delete selected tweets") {
      statusTableModel.removeSelectedElements(getSelectedModelIndexes) }
    val bs  = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0)
    val del = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)
    deleteAction.accelerator = Some(bs)
    connectAction(deleteAction, bs, del)
    actions ::= deleteAction
    
    actions
  }

  private def connectAction(a: Action, keys: KeyStroke*) {
    getActionMap.put(a.title, a.peer)
    for (key <- keys) getInputMap.put(key, a.title)
  }

}