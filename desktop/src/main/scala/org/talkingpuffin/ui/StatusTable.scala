package org.talkingpuffin.ui

import java.awt.{Toolkit}
import _root_.scala.{Option}
import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.event.KeyEvent._
import java.beans.PropertyChangeEvent
import javax.swing.event._
import javax.swing.table.{DefaultTableCellRenderer}
import javax.swing.KeyStroke.{getKeyStroke => ks}
import javax.swing.{JMenu}
import scala.swing.{Reactor, Action}
import com.google.common.collect.Lists
import org.jdesktop.swingx.decorator.{SortKey, SortOrder, HighlighterFactory}
import org.jdesktop.swingx.event.TableColumnModelExtListener
import org.jdesktop.swingx.JXTable
import org.talkingpuffin.state.GlobalPrefs.PrefChangedEvent
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.talkingpuffin.ui.table.{EmphasizedStringCellRenderer, EmphasizedStringComparator, StatusCellRenderer}
import org.talkingpuffin.util.{Loggable, PopupListener}
import org.talkingpuffin.twitter.{TwitterStatus}
import org.talkingpuffin.Session
import java.net.{HttpURLConnection, URL}
import io.Source
import util.{LinkUnIndirector, ShortUrl, TableUtil, DesktopUtil}

/**
 * Table of statuses.
 */
class StatusTable(session: Session, tableModel: StatusTableModel, showBigPicture: => Unit)
    extends JXTable(tableModel) with Loggable {

  setColumnControlVisible(true)
  val rowMarginVal = 3
  setRowMargin(rowMarginVal)
  setHighlighters(HighlighterFactory.createSimpleStriping)

  private val userActions = new UserActions(session, tableModel.relationships)
  
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

  private val mh = new PopupMenuHelper(this)
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

  addMouseListener(new PopupListener(this, mh.menu))
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) reply
  })
  getSelectionModel.addListSelectionListener(new ListSelectionListener {
    def valueChanged(event: ListSelectionEvent) = 
      if (! event.getValueIsAdjusting) 
        specialMenuItems.enableActions(getSelectedStatuses, getSelectedScreenNames.length, 
          tableModel.relationships.friendIds, tableModel.relationships.followerIds)
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
 
  private def viewSourceSelected = getSelectedStatuses.foreach(status => 
    DesktopUtil.browse("http://twitter.com/statuses/show/" + status.id + ".xml"))
 
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
  
  private def browseLink(uri: String) = LinkUnIndirector.browse(uri) 

  private def retweetNewWay = getSelectedStatuses.foreach(status => session.twitterSession.retweet(status.id))
  
  private def createSendMsgDialog(status: TwitterStatus, names: Option[String], retweetMsg: Option[String]) =
    new SendMsgDialog(session, null, names, Some(status.id), retweetMsg, false)
  
  private def getSelectedScreenNames = getSelectedStatuses.map(_.user.screenName).removeDuplicates
  def getSelectedStatuses = tableModel.getStatuses(TableUtil.getSelectedModelIndexes(this))

  def getSelectedStatus: Option[TwitterStatus] = {
    val row = getSelectedRow
    if (row == -1) None else Some(tableModel.getStatusAt(convertRowIndexToModel(row)))
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
    val SHORTCUT = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
    val SHIFT = java.awt.event.InputEvent.SHIFT_DOWN_MASK

    mh add new NextTAction(this)
    mh add new PrevTAction(this)
    
    mh add(Action("Reply…") { reply }, ks(VK_R, 0))
    mh add(new Action("Direct Message…") { 
      def apply = dm(getSelectedScreenNames(0))
      specialMenuItems.oneScreennameSelected.list ::= this
      specialMenuItems.followersOnly.list ::= this
    }, ks(VK_D, 0))
    mh add(new Action("Retweet old way…") { 
      def apply = retweetOldWay
      specialMenuItems.oneStatusSelected.list ::= this
    }, ks(VK_E, 0))
    mh add(Action("Retweet new way") { retweetNewWay }, ks(VK_E, SHORTCUT))
    
    mh add(new TagAction(this, tableModel), ks(VK_T, 0))
    mh add(new Action("Show larger image") { 
      def apply = showBigPicture
      specialMenuItems.oneStatusSelected.list ::= this
    }, ks(VK_I, 0))
    
    mh.menu.add(new JMenu("View in browser") {
      mh add(Action("Status") {viewSelected}, this, ks(VK_V, 0))
      mh add(Action("Status source") {viewSourceSelected}, this, ks(VK_V, SHORTCUT | SHIFT))
      mh add(Action("Sender") {viewUser}, this, ks(VK_V, SHIFT))
    })
    
    mh add(Action("Edit user properties…") {editUser}, ks(VK_P, SHORTCUT))

    mh add(new OpenPageLinksAction(getSelectedStatus, this, browseLink), ks(VK_L, 0))
    mh add(new OpenTwitterUserLinksAction(getSelectedStatus, this, DesktopUtil.browse), ks(VK_U, 0))
    mh add(new OpenTwitterUserListsAction(getSelectedStatus, this, DesktopUtil.browse), ks(VK_U, SHIFT))
    
    mh add(Action("Show friends and followers") 
        {userActions.showFriends(getSelectedScreenNames)}, UserActions.ShowFriendsAccel)
    mh add(Action("View lists…") {userActions.viewLists(getSelectedScreenNames, this)}, UserActions.ViewListAccel)
    
    mh.menu.add(new JMenu("Mute") {
      mh add(Action("User") {tableModel.muteSelectedUsers(
        TableUtil.getSelectedModelIndexes(StatusTable.this))}, this, ks(VK_M, SHORTCUT))
      mh add(Action("Retweets by user") {tableModel.muteSelectedUsersRetweets(
        TableUtil.getSelectedModelIndexes(StatusTable.this))}, this, ks(VK_M, SHORTCUT | SHIFT))
      mh add(Action("Application") {tableModel.muteSelectedApps(
        TableUtil.getSelectedModelIndexes(StatusTable.this))}, this, ks(VK_A, SHORTCUT | SHIFT))
    })
    
    mh.menu.add(new JMenu("Size") {
      mh add(Action("Increase Font Size") { changeFontSize(5) }, this, ks(VK_O, SHORTCUT))
      mh add(Action("Decrease Font Size") { changeFontSize(-5) }, this, ks(VK_O, SHORTCUT | SHIFT))
      mh add(Action("Increase Row Height") { changeRowHeight(8) }, this)
      mh add(Action("Decrease Row Height") { changeRowHeight(-8) }, this)
    })
    
    mh add(new Action("Follow") { 
      def apply = userActions.follow(getSelectedScreenNames)
      specialMenuItems.notFriendsOnly.list ::= this
    }, UserActions.FollowAccel)
    mh add(new Action("Unfollow") {
      def apply = userActions.unfollow(getSelectedScreenNames)
      specialMenuItems.friendsOnly.list ::= this
    }, UserActions.UnfollowAccel)
    mh add(Action("Block") { userActions.block(getSelectedScreenNames) }, UserActions.BlockAccel)
    mh.add(Action("Report Spam") { userActions.reportSpam(getSelectedScreenNames) }, UserActions.ReportSpamAccel)
    
    mh.menu.add(new JMenu("Delete") {
      mh add(Action("Selected tweets") {
        tableModel removeStatuses TableUtil.getSelectedModelIndexes(StatusTable.this) 
      }, this, ks(VK_D, SHORTCUT), ks(VK_DELETE, 0), ks(VK_BACK_SPACE, 0))
  
      mh add(Action("All tweets from selected users") {
        tableModel removeStatusesFrom getSelectedScreenNames }, this, ks(VK_D, SHORTCUT | SHIFT))
    })
  }

  private def changeFontSize(change: Int) {
    statusTextSize += change
    tableModel.fireTableDataChanged  
  }
  
  private def changeRowHeight(change: Int) = customRowHeight += change
  
  private def thumbnailHeight = Thumbnail.THUMBNAIL_SIZE + rowMarginVal + 2
  
}

