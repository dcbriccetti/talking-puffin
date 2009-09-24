package org.talkingpuffin.ui

import com.google.common.collect.Lists
import java.awt.{Toolkit}
import _root_.scala.{Option}
import java.awt.event.{KeyEvent, MouseEvent, MouseAdapter}
import java.beans.PropertyChangeEvent
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer}
import javax.swing.{KeyStroke, JPopupMenu}
import jdesktop.swingx.decorator.{SortKey, SortOrder, HighlighterFactory}
import org.jdesktop.swingx.event.TableColumnModelExtListener
import org.jdesktop.swingx.JXTable
import state.GlobalPrefs.PrefChangedEvent
import state.{PrefKeys, GlobalPrefs}
import swing.{Reactor, MenuItem, Action}
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
  
  private var customRowHeight_ = 0
  private def customRowHeight = customRowHeight_
  private def customRowHeight_=(value: Int) = {
    customRowHeight_ = value
    setRowHeight(customRowHeight_)
    GlobalPrefs.prefs.putInt(PrefKeys.STATUS_TABLE_ROW_HEIGHT, value)
  }
  customRowHeight = GlobalPrefs.prefs.getInt(PrefKeys.STATUS_TABLE_ROW_HEIGHT, thumbnailHeight) 
  
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
  private var specialMenuItems = new SpecialMenuItems
  buildActions

  new Reactor {
    listenTo(GlobalPrefs.publisher)
    reactions += { case e: PrefChangedEvent => 
      if (e.key == PrefKeys.SHOW_TWEET_DATE_AS_AGE) {  
        // Isn’t working    ageCol.setTitle("hi")
      }
    }
  }

  addMouseListener(new PopupListener(this, new PopupMenu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) reply
  })
  getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(event: ListSelectionEvent) = 
      if (! event.getValueIsAdjusting) 
        specialMenuItems.enableActions(getSelectedStatuses, getSelectedScreenNames.length, 
          session.windows.streams.friendIds, session.windows.streams.followerIds)
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
  
  private def viewSelected = getSelectedStatuses.foreach(status => 
    DesktopUtil.browse("http://twitter.com/" + status.user.screenName + "/statuses/" + status.id))
 
  private def viewUser = getSelectedScreenNames.foreach(screenName => 
    DesktopUtil.browse("http://twitter.com/" + screenName))

  private def editUser = getSelectedStatuses.foreach(status => { 
    val userProperties = new UserPropertiesDialog(session.userPrefs, status)
    userProperties.visible = true  
  })

  private def statusTextSize = statusCellRenderer.textSizePct
  private def statusTextSize_=(sizePct: Int) = {
    statusCellRenderer.textSizePct = sizePct
    GlobalPrefs.prefs.putInt(PrefKeys.STATUS_TABLE_STATUS_FONT_SIZE, sizePct)
  }
  
  def reply {
    val statuses = getSelectedStatuses
    val recipients = statuses.map(("@" + _.user.screenName)).mkString(" ")
    createSendMsgDialog(statuses(0), Some(recipients), None).visible = true
  }
  
  def dm(screenName: String) =
    (new SendMsgDialog(session, null, Some(screenName), None, None, true)).visible = true
  
  private def retweetOldWay {
    val status = getSelectedStatuses(0) 
    val name = "@" + status.user.screenName
    createSendMsgDialog(status, Some(name), Some(status.text)).visible = true
  }

  private def retweetNewWay = getSelectedStatuses.foreach(status => session.twitterSession.retweet(status.id))
  
  private def createSendMsgDialog(status: TwitterStatus, names: Option[String], retweetMsg: Option[String]) =
    new SendMsgDialog(session, null, names, Some(status.id), retweetMsg, false)
  
  private def follow   = getSelectedScreenNames foreach session.twitterSession.createFriendship
  private def unfollow = getSelectedScreenNames foreach session.twitterSession.destroyFriendship
  private def block    = getSelectedScreenNames foreach session.twitterSession.blockUser
  private def unblock  = getSelectedScreenNames foreach session.twitterSession.unblockUser
  private def getSelectedScreenNames = getSelectedStatuses.map(_.user.screenName).removeDuplicates
  def getSelectedStatuses = tableModel.getStatuses(TableUtil.getSelectedModelIndexes(this))

  def getSelectedStatus: Option[TwitterStatus] = {
    val row = getSelectedRow
    if (row == -1) None else Some(tableModel.getStatusAt(convertRowIndexToModel(row)))
  }

  class PopupMenu extends JPopupMenu {
    ap.actions.reverse.foreach(a => add(new MenuItem(a).peer))
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

  protected def buildActions {
    val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
    val SHIFT = java.awt.event.InputEvent.SHIFT_DOWN_MASK

    ap add(Action("View status in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap add(Action("View user in Browser") {viewUser}, KeyStroke.getKeyStroke(KeyEvent.VK_V, SHIFT))
    ap add(Action("Edit user properties…") {editUser}, KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutKeyMask))
    ap add(new OpenPageLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_L))
    ap add(new OpenTwitterUserLinksAction(getSelectedStatus, this, DesktopUtil.browse), Actions.ks(KeyEvent.VK_U))
    ap add(Action("Mute") {tableModel.muteSelectedUsers(TableUtil.getSelectedModelIndexes(this))}, 
      KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutKeyMask))
    ap add(Action("Mute Retweets") {tableModel.muteSelectedUsersRetweets(TableUtil.getSelectedModelIndexes(this))}, 
      KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutKeyMask | SHIFT))
    ap add new NextTAction(this)
    ap add new PrevTAction(this)
    ap add(new TagAction(this, tableModel), Actions.ks(KeyEvent.VK_T))
    ap add(Action("Increase Font Size") { changeFontSize(5) }, 
        KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask))
    ap add(Action("Decrease Font Size") { changeFontSize(-5) }, 
        KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask | SHIFT))
    ap add(Action("Increase Row Height") { changeRowHeight(8) })
    ap add(Action("Decrease Row Height") { changeRowHeight(-8) })
    var action = Action("Show Larger Image") { showBigPicture }
    ap add(action, Actions.ks(KeyEvent.VK_I))
    specialMenuItems.oneStatusSelected.list ::= action
    ap add(Action("Reply…") { reply }, Actions.ks(KeyEvent.VK_R))
    action = Action("Direct Message…") { dm(getSelectedScreenNames(0)) }
    ap add(action, Actions.ks(KeyEvent.VK_D))
    specialMenuItems.oneScreennameSelected.list ::= action
    specialMenuItems.followersOnly.list ::= action
    action = Action("Retweet old way…") { retweetOldWay }
    ap add(action, Actions.ks(KeyEvent.VK_E))
    specialMenuItems.oneStatusSelected.list ::= action
    ap add(Action("Retweet new way") { retweetNewWay }, KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutKeyMask))
    action = Action("Follow") { follow }
    specialMenuItems.notFriendsOnly.list ::= action
    ap add(action, KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask))
    action = Action("Unfollow") { unfollow }
    specialMenuItems.friendsOnly.list ::= action
    ap add(action, KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask | SHIFT))
    ap add(Action("Block") { block }, KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask))
    
    ap add(Action("Delete selected tweets") {
      tableModel removeStatuses TableUtil.getSelectedModelIndexes(this) 
    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcutKeyMask),
      Actions.ks(KeyEvent.VK_DELETE), Actions.ks(KeyEvent.VK_BACK_SPACE))

    ap add(Action("Delete all tweets from all selected users") {
      tableModel removeStatusesFrom getSelectedScreenNames 
    }, KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcutKeyMask | SHIFT))
  }

  private def changeFontSize(change: Int) {
    statusTextSize += change
    tableModel.fireTableDataChanged  
  }
  
  private def changeRowHeight(change: Int) = customRowHeight += change
  
  private def thumbnailHeight = Thumbnail.THUMBNAIL_SIZE + rowMarginVal + 2
}

