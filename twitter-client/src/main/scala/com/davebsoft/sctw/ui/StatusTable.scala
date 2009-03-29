package com.davebsoft.sctw.ui

import _root_.scala.swing.GridBagPanel._
import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.swing.{MenuItem, Action}
import java.awt.{Desktop, Toolkit, Component, Font}
import _root_.scala.xml.{NodeSeq, Node}

import _root_.scala.{Option}
import java.awt.event.{KeyEvent, ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.net.{URI, URL}
import java.util.Comparator
import java.util.regex.Pattern
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableCellRenderer}
import filter.TagsRepository
import javax.swing.{JTable, KeyStroke, JMenu, JMenuItem, JPopupMenu, JComponent}
import twitter.Sender
import util.{TableUtil, DesktopUtil}
/**
 * Table of statuses.
 * @author Dave Briccetti
 */

class StatusTable(statusTableModel: StatusTableModel, apiHandlers: ApiHandlers,
      clearAction: Action, showBigPicture: => Unit) 
    extends JTable(statusTableModel) {

  setRowHeight(Thumbnail.THUMBNAIL_SIZE + 2)
  val sorter = new TableRowSorter[StatusTableModel](statusTableModel)
  sorter.setComparator(2, new Comparator[FromTo] {
    def compare(o1: FromTo, o2: FromTo) = o1.from.compareToIgnoreCase(o2.from)
  })
  setRowSorter(sorter)
  
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer with ZebraStriping)

  configureColumns

  val ap = new ActionPrep(this)
  buildActions

  addMouseListener(new PopupListener(this, getPopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) reply
  })
  
  def viewSelected {
    getSelectedStatus match {
      case Some(status) =>
        var uri = "http://twitter.com/" +
                (status \ "user" \ "screen_name").text + "/statuses/" + (status \ "id").text
        DesktopUtil.browse(uri)
      case None =>
    }
  }
  
  def reply {
    getSelectedStatus match {
      case Some(s) => 
        val sm = new SendMsgDialog(null, apiHandlers.sender,
          Some((s \ "user" \ "screen_name").text), Some((s \ "id").text))
        sm.visible = true
      case None =>
    }
  }
  
  private def unfollow {
    getSelectedStatuses.foreach(status => apiHandlers.follower.unfollow((status \ "user" \ "screen_name").text))
  }
  
  def getSelectedStatuses: List[Node] = {
    statusTableModel.getStatuses(TableUtil.getSelectedModelIndexes(this))
  }

  def getSelectedStatus: Option[NodeSeq] = {
    val row = getSelectedRow
    if (row == -1) None else Some(statusTableModel.getStatusAt(convertRowIndexToModel(row)))
  }

  def getPopupMenu: JPopupMenu = {
    val menu = new JPopupMenu

    for (action <- ap.actions.reverse) 
      menu.add(new MenuItem(action).peer)

    val tagAl = new ActionListener() {
      def actionPerformed(e: ActionEvent) = {
        statusTableModel.tagSelectedUsers(TableUtil.getSelectedModelIndexes(StatusTable.this), e.getActionCommand)
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
  
  private def configureColumns {
    val colModel = getColumnModel
    
    val picCol = colModel.getColumn(0)
    picCol.setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
    
    val ageCol = colModel.getColumn(1)
    ageCol.setPreferredWidth(60)
    ageCol.setMaxWidth(100)
    ageCol.setCellRenderer(new AgeCellRenderer)
    
    val nameCol = colModel.getColumn(2)
    nameCol.setPreferredWidth(100)
    nameCol.setMaxWidth(200)
    nameCol.setCellRenderer(new FromToCellRenderer)
    
    val statusCol = colModel.getColumn(3)
    statusCol.setPreferredWidth(600)
    statusCol.setCellRenderer(new WordWrappingCellRenderer {
      val normalFont = getFont
      setFont(new Font(normalFont.getFontName, Font.PLAIN, normalFont.getSize * 120 / 100))
    })
  }

  protected def buildActions = {
    ap.addAction(Action("View in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap.addAction(new OpenLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_L))
    ap.addAction(Action("Mute") {statusTableModel.muteSelectedUsers(TableUtil.getSelectedModelIndexes(this))}, 
      Actions.ks(KeyEvent.VK_M))
    ap.addAction(new NextTAction(this))
    ap.addAction(new PrevTAction(this))
    ap.addAction(Action("Show Larger Image") { showBigPicture }, Actions.ks(KeyEvent.VK_I))
    ap.addAction(Action("Reply") { reply }, Actions.ks(KeyEvent.VK_R))
    ap.addAction(Action("Unfollow") { unfollow }, KeyStroke.getKeyStroke(KeyEvent.VK_U, 
      Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
  }

}

class TweetsTable(statusTableModel: StatusTableModel, apiHandlers: ApiHandlers,
    clearAction: Action, showBigPicture: => Unit) 
    extends StatusTable(statusTableModel, apiHandlers, clearAction, showBigPicture) {
  
  override def buildActions {
    super.buildActions
    ap.addAction(clearAction, Actions.ks(KeyEvent.VK_C))
    val deleteTitle = "Delete selected tweets"
    ap.addAction(Action(deleteTitle) {
      statusTableModel.removeSelectedElements(TableUtil.getSelectedModelIndexes(this)) 
    }, Actions.ks(KeyEvent.VK_DELETE), Actions.ks(KeyEvent.VK_BACK_SPACE))  
  }

  
}