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

class StatusTable(statusTableModel: StatusTableModel, sender: Sender,
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
  statusCol.setCellRenderer(new StatusCellRenderer)

  var actions = List[Action]()
  buildActions

  addMouseListener(new PopupListener(this, getPopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = {
      if (e.getClickCount == 2) {
        viewSelected
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
  
  protected def getSelectedModelIndexes: List[Int] = {
    val tableRows = getSelectedRows
    var smi = List[Int]()
    for (i <- 0 to (tableRows.length - 1)) {
      smi ::= convertRowIndexToModel(tableRows(i))
    }
    smi
  }

  protected def addAction(action: Action, key: KeyStroke) {
    action.accelerator = Some(key)
    getActionMap.put(action.title, action.peer)
    connectAction(action, key)
    actions ::= action
  }

  protected def ks(keyEvent: Int): KeyStroke = KeyStroke.getKeyStroke(keyEvent, 0)

  protected def buildActions = {
    addAction(Action("View in Browser") {viewSelected}, ks(KeyEvent.VK_V))
    addAction(new OpenLinksAction(getSelectedStatus, this, browse), ks(KeyEvent.VK_L))
    addAction(Action("Mute") {statusTableModel.muteSelectedUsers(getSelectedModelIndexes)}, ks(KeyEvent.VK_M))
    addAction(Action("Next") {
      dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
        0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED))
    }, ks(KeyEvent.VK_N))
    addAction(Action("Previous") {
      dispatchEvent(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
        0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED))
    }, ks(KeyEvent.VK_P))
    addAction(Action("Show Larger Image") { showBigPicture }, ks(KeyEvent.VK_I))
    addAction(Action("Reply") { reply }, ks(KeyEvent.VK_R))
  }

  private def connectAction(a: Action, keys: KeyStroke*) {
    getActionMap.put(a.title, a.peer)
    for (key <- keys) getInputMap.put(key, a.title)
  }

}

class TweetsTable(statusTableModel: StatusTableModel, sender: Sender,
    clearAction: Action, showBigPicture: => Unit) 
    extends StatusTable(statusTableModel, sender, clearAction, showBigPicture) {
  
  override def buildActions {
    super.buildActions
    addAction(clearAction, ks(KeyEvent.VK_C))
    val deleteTitle = "Delete selected tweets"
    addAction(Action(deleteTitle) {
      statusTableModel.removeSelectedElements(getSelectedModelIndexes) }, ks(KeyEvent.VK_BACK_SPACE))
    getInputMap.put(ks(KeyEvent.VK_DELETE), deleteTitle)  
  }

  
}