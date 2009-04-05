package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.xml.{NodeSeq, Node}
import filter.TagUsers
import java.awt.event.{ActionListener, ActionEvent, KeyEvent}
import javax.swing.{JTable, KeyStroke, Icon, JPopupMenu}
import java.awt.{Toolkit, Font}
import java.util.Comparator
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, AbstractTableModel}
import org.jdesktop.swingx.decorator.{HighlighterFactory, Highlighter}
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.table.TableColumnExt
import scala.swing._
import scala.swing.GridBagPanel._
import twitter.{FriendsFollowersDataProvider}
import util.{TableUtil, DesktopUtil}
/**
 * Displays a list of friends or followers
 */

object UserColumns {
  val ARROWS = 0
  val PICTURE = 1
  val SCREEN_NAME = 2
  val NAME = 3
  val TAGS = 4
  val LOCATION = 5
  val DESCRIPTION = 6
  val STATUS = 7
}

class FriendsFollowersPane(apiHandlers: ApiHandlers, friends: List[Node], followers: List[Node]) extends GridBagPanel {
  val model = new UsersModel(friends, followers)
  var table: JTable = _
  val tableScrollPane = new ScrollPane {
    table = new JXTable(model) {
      setColumnControlVisible(true)
      setHighlighters(HighlighterFactory.createSimpleStriping)
      setRowHeight(Thumbnail.THUMBNAIL_SIZE + 2)
      setDefaultRenderer(classOf[String], new DefaultTableCellRenderer)
      
      List(UserColumns.NAME, UserColumns.TAGS, UserColumns.LOCATION, UserColumns.STATUS, 
        UserColumns.DESCRIPTION).foreach(i => {
        getColumnModel.getColumn(i).setCellRenderer(new WordWrappingCellRenderer)
      })
      
      getColumnModel.getColumn(UserColumns.ARROWS).setPreferredWidth(20)
      getColumnModel.getColumn(UserColumns.ARROWS).setMaxWidth(30)
      getColumnModel.getColumn(UserColumns.PICTURE).setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
      val screenNameCol = getColumnModel.getColumn(UserColumns.SCREEN_NAME).asInstanceOf[TableColumnExt]
      screenNameCol.setCellRenderer(new EmphasizedStringCellRenderer)
      screenNameCol.setComparator(EmphasizedStringComparator)
      getColumnModel.getColumn(UserColumns.DESCRIPTION).setPreferredWidth(250)
      getColumnModel.getColumn(UserColumns.STATUS).setPreferredWidth(250)
      val ap = new ActionPrep(this)
      buildActions(ap, this)
      addMouseListener(new PopupListener(this, getPopupMenu(ap)))
    }
    peer.setViewportView(table)
  }
  add(new Label("Following: " + friends.size + ", Followers: " + followers.size +
    ", Overlap: " + (friends.size + followers.size - model.combined.size)),
    new Constraints { grid=(0,0); anchor=Anchor.West })
  
  add(tableScrollPane, new Constraints { 
    grid=(0,1); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1 
  })

  private def buildActions(ap: ActionPrep, comp: java.awt.Component) = {
    ap.addAction(Action("View in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap.addAction(new NextTAction(comp))
    ap.addAction(new PrevTAction(comp))
    ap.addAction(Action("Reply") { reply }, Actions.ks(KeyEvent.VK_R))
    ap.addAction(Action("Unfollow") { unfollow }, KeyStroke.getKeyStroke(KeyEvent.VK_U, 
      Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
  }

  private def getPopupMenu(ap: ActionPrep): JPopupMenu = {
    val menu = new JPopupMenu
    for (action <- ap.actions.reverse) 
      menu.add(new MenuItem(action).peer)
    menu
  }
  
  private def getSelectedUsers = TableUtil.getSelectedModelIndexes(table).map(model.combined(_))
  
  def getSelectedScreenNames: List[String] = {
    getSelectedUsers.map(user => (user \ "screen_name").text)
  }

  private def unfollow = getSelectedScreenNames foreach apiHandlers.follower.unfollow
  
  private def viewSelected {
    getSelectedUsers.foreach(user => {
      var uri = "http://twitter.com/" + (user \ "screen_name").text
      DesktopUtil.browse(uri)
    })
  }
  
  private def reply {
    val names = getSelectedUsers.map(user => ("@" + (user \ "screen_name").text)).mkString(" ")
    val sm = new SendMsgDialog(null, apiHandlers.sender, Some(names), None)
    sm.visible = true
  }
  
}

class UsersModel(friends: List[Node], followers: List[Node]) extends AbstractTableModel {
  private val colNames = List(" ", "Image", "Screen Name", "Name", "Tags", "Location", "Description", "Status")
  private val elementNames = List("", "", "screen_name", "name", "", "location", "description", "")
  private val set = scala.collection.mutable.Set[Node]()
  set ++ friends
  set ++ followers
  val combinedList = set.toList.sort((a,b) => 
    ((a \ "name").text.toLowerCase compareTo (b \ "name").text.toLowerCase) < 0)
  val combined = combinedList.toArray
  val arrows = combinedList.map(user => {
    val friend = friends.contains(user)
    val follower = followers.contains(user)
    if (friend && follower) "↔" else if (friend) "→" else "←"
  }).toArray
  
  def getColumnCount = 8
  def getRowCount = combined.length

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case UserColumns.PICTURE => classOf[Icon]
      case _ => classOf[String] 
    }
  }

  val pcell = new PictureCell(this, 1)

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = combined(rowIndex)
    def colVal = (user \ elementNames(columnIndex)).text
    columnIndex match {
      case UserColumns.PICTURE => {
        val picUrl = (user \ "profile_image_url").text
        pcell.request(picUrl, rowIndex)
      }
      case UserColumns.ARROWS => arrows(rowIndex)
      case UserColumns.SCREEN_NAME => new EmphasizedString(Some(colVal), followers.contains(user))
      case UserColumns.TAGS => TagUsers.tagsForUser((user \ "id").text).mkString(", ")
      case UserColumns.STATUS => (user \ "status" \ "text").text
      case _ => colVal
    }
  }
  override def getColumnName(column: Int) = colNames(column)
}

