package org.talkingpuffin.ui

import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.swing.{MenuItem, Action}
import apache.log4j.Logger
import com.google.common.collect.Lists
import java.awt.{Desktop, Toolkit, Component, Font}

import _root_.scala.{Option}
import java.awt.event.{KeyEvent, ActionEvent, ActionListener, MouseEvent, MouseAdapter}
import java.awt.image.BufferedImage
import java.beans.PropertyChangeEvent
import java.net.{URI, URL}
import java.util.Comparator
import java.util.regex.Pattern
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, TableColumnModel, TableCellRenderer, DefaultTableColumnModel}
import javax.swing.{JTable, KeyStroke, JMenu, JMenuItem, JPopupMenu, JComponent}
import jdesktop.swingx.decorator.{SortKey, SortOrder, HighlighterFactory}
import org.jdesktop.swingx.event.TableColumnModelExtListener
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.table.{TableColumnModelExt, TableColumnExt}
import state.{PrefKeys, GlobalPrefs}
import table.{EmphasizedStringCellRenderer, EmphasizedStringComparator, StatusCellRenderer}
import talkingpuffin.util.{Loggable, PopupListener}
import twitter.{TwitterStatus}
import util.{TableUtil, DesktopUtil}

/**
 * Table of statuses.
 */
class StatusTable(session: Session, tableModel: StatusTableModel, showBigPicture: => Unit)
    extends JXTable(tableModel) with Loggable {

  setColumnControlVisible(true)
  val rowMarginVal = 3
  setRowMargin(rowMarginVal)
  setHighlighters(HighlighterFactory.createSimpleStriping)
  setRowHeight(Thumbnail.THUMBNAIL_SIZE + rowMarginVal + 2)
  
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer)
  val statusCellRenderer = new StatusCellRenderer
  statusCellRenderer.textSizePct = GlobalPrefs.prefs.getInt(PrefKeys.STATUS_TABLE_STATUS_FONT_SIZE, 100)

  val ageCol   = getColumnExt(0)
  val imageCol = getColumnExt(1)
  val nameCol  = getColumnExt(2)
  val toCol    = getColumnExt(3)
  val colAndKeys = List(ageCol, imageCol, nameCol, toCol) zip
    List(PrefKeys.AGE, PrefKeys.IMAGE, PrefKeys.FROM, PrefKeys.TO)
  configureColumns

  val ap = new ActionPrep(this)
  buildActions

  addMouseListener(new PopupListener(this, new PopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) reply
  })
  
  def saveState {
    val sortKeys = getSortController.getSortKeys
    if (sortKeys.size > 0) {
      val sortKey = sortKeys.get(0)
      val sortCol = getColumns(true).get(sortKey.getColumn)
      for ((col, key) <- colAndKeys; if (col == sortCol)) {
        GlobalPrefs.sortBy(key, if (sortKey.getSortOrder == SortOrder.DESCENDING) 
          PrefKeys.SORT_DIRECTION_DESC else PrefKeys.SORT_DIRECTION_ASC)
      }
    }
  }
  
  private def viewSelected {
    getSelectedStatuses.foreach(status => {
      var uri = "http://twitter.com/" +
          status.user.screenName + "/statuses/" + status.id
      DesktopUtil.browse(uri)
    })
  }
 
  private def viewUser = getSelectedScreenNames.foreach(screenName => 
      DesktopUtil.browse("http://twitter.com/" + screenName))

  private def editUser = getSelectedStatuses.foreach(status => { 
    val userProperties = new UserPropertiesDialog(session.windows.streams.prefs, status)
    userProperties.visible = true  
  })

  def statusTextSize = statusCellRenderer.textSizePct
  def statusTextSize_=(sizePct: Int) = {
    statusCellRenderer.textSizePct = sizePct
    GlobalPrefs.prefs.putInt(PrefKeys.STATUS_TABLE_STATUS_FONT_SIZE, sizePct)
  }
  
  def reply {
    val statuses = getSelectedStatuses
    if (statuses.length > 0) {
      val recipients = statuses.map(status => ("@" + status.user.screenName)).mkString(" ")
      createSendMsgDialog(statuses(0), Some(recipients), None).visible = true
    }
  }
  
  def retweet {
    val statuses = getSelectedStatuses
    if (statuses.length == 1 )  {
      val status = statuses(0) 
      val name = "@" + status.user.screenName
      createSendMsgDialog(status, Some(name), Some(status.text)).visible = true
    }
  }
  
  private def createSendMsgDialog(status: TwitterStatus, names: Option[String], retweetMsg: Option[String]) =
    new SendMsgDialog(session, null, names, 
        Some(status.id.toString()), retweetMsg)
  
  private def unfollow = getSelectedScreenNames foreach session.twitterSession.destroyFriendship
  private def block = getSelectedScreenNames foreach session.twitterSession.blockUser
  private def unblock = getSelectedScreenNames foreach session.twitterSession.unblockUser

  def getSelectedScreenNames = getSelectedStatuses.map(_.user.screenName).removeDuplicates
  def getSelectedStatuses = tableModel.getStatuses(TableUtil.getSelectedModelIndexes(this))

  def getSelectedStatus: Option[TwitterStatus] = {
    val row = getSelectedRow
    if (row == -1) None else Some(tableModel.getStatusAt(convertRowIndexToModel(row)))
  }

  class PopupMenu extends JPopupMenu {
    for (action <- ap.actions.reverse) 
      add(new MenuItem(action).peer)
  }
  
  private def configureColumns {
    ageCol.setPreferredWidth(60)
    ageCol.setMaxWidth(100)
    ageCol.setCellRenderer(new AgeCellRenderer)
    
    imageCol.setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
    imageCol.setSortable(false)
    
    nameCol.setPreferredWidth(100)
    nameCol.setMaxWidth(200)
    nameCol.setCellRenderer(new EmphasizedStringCellRenderer)
    nameCol.setComparator(EmphasizedStringComparator)
    
    toCol.setPreferredWidth(100)
    toCol.setMaxWidth(200)
    toCol.setCellRenderer(new EmphasizedStringCellRenderer)
    toCol.setComparator(EmphasizedStringComparator)
    
    val statusCol = getColumnExt(4)
    statusCol.setPreferredWidth(600)
    statusCol.setCellRenderer(statusCellRenderer)
    statusCol.setSortable(false)

    // Set from preferences
    
    for ((col, key) <- colAndKeys) {
      col.setVisible(GlobalPrefs.isColumnShowing(key))
      updateTableModelOptions(col)
    }

    val sortDir = if (GlobalPrefs.prefs.get(PrefKeys.SORT_DIRECTION, PrefKeys.SORT_DIRECTION_DESC) == 
            PrefKeys.SORT_DIRECTION_DESC) SortOrder.DESCENDING else SortOrder.ASCENDING
    val modelIndex = colAndKeys.find(ck => ck._2 ==
            GlobalPrefs.prefs.get(PrefKeys.SORT_BY, PrefKeys.AGE)).get._1.getModelIndex
    getSortController.setSortKeys(Lists.newArrayList(new SortKey(sortDir, modelIndex)))

    getColumnModel.addColumnModelListener(new TableColumnModelExtListener {
      def columnPropertyChange(event: PropertyChangeEvent) = {
        if (event.getPropertyName.equals("visible")) {
          // Save changes into preferences.
          val source = event.getSource
          updateTableModelOptions(source)

          for ((col, key) <- colAndKeys; if (source == col)) GlobalPrefs.showColumn(key, col.isVisible)
        }
      }

      def columnSelectionChanged(e: ListSelectionEvent) = {}
      def columnRemoved(e: TableColumnModelEvent) = {}
      def columnMoved(e: TableColumnModelEvent) = {}
      def columnMarginChanged(e: ChangeEvent) = {}
      def columnAdded(e: TableColumnModelEvent) = {}
    })
  }

  /**
   * Table model needs to know if certain cols are hidden, to put their contents in the status if so.
   */
  private def updateTableModelOptions(source: Object) {
    val op = tableModel.options
    if      (source == ageCol)  op.showAgeColumn  = ageCol .isVisible
    else if (source == nameCol) op.showNameColumn = nameCol.isVisible
    else if (source == toCol)   op.showToColumn   = toCol  .isVisible
  }

  protected def buildActions = {
    val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

    ap add(Action("View status in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap add(Action("View user in Browser") {viewUser}, KeyStroke.getKeyStroke(KeyEvent.VK_V, 
      java.awt.event.InputEvent.SHIFT_DOWN_MASK))  
    ap add(Action("Edit user properties…") {editUser}, KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutKeyMask))
    ap add(new OpenPageLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_L))
    ap add(new OpenTwitterUserLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_U))
    ap add(Action("Mute") {tableModel.muteSelectedUsers(TableUtil.getSelectedModelIndexes(this))}, 
      Actions.ks(KeyEvent.VK_M))
    ap add new NextTAction(this)
    ap add new PrevTAction(this)
    ap add(new TagAction(this, tableModel), Actions.ks(KeyEvent.VK_T))
    ap add(Action("Increase Font Size") { changeFontSize(5) }, 
        KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask))
    ap add(Action("Decrease Font Size") { changeFontSize(-5) }, 
        KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask | 
      java.awt.event.InputEvent.SHIFT_DOWN_MASK))
    ap add(Action("Show Larger Image") { showBigPicture }, Actions.ks(KeyEvent.VK_I))
    ap add(Action("Reply…") { reply }, Actions.ks(KeyEvent.VK_R))
    ap add(Action("Retweet") { retweet }, Actions.ks(KeyEvent.VK_E))
    ap add(Action("Unfollow") { unfollow}, KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcutKeyMask))
    ap add(Action("Block") { block }, KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask))
    
    ap add(Action("Delete selected tweets") {
      tableModel removeStatuses TableUtil.getSelectedModelIndexes(this) 
    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcutKeyMask),
      Actions.ks(KeyEvent.VK_DELETE), Actions.ks(KeyEvent.VK_BACK_SPACE))

    ap add(Action("Delete all tweets from all selected users") {
      tableModel removeStatusesFrom getSelectedScreenNames 
    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcutKeyMask | 
      java.awt.event.InputEvent.SHIFT_DOWN_MASK))  
  }

  private def changeFontSize(change: Int) {
    statusTextSize += change
    tableModel.fireTableDataChanged  
  }
  
}

