package com.davebsoft.sctw.ui

import _root_.com.davebsoft.sctw.util.PopupListener
import _root_.scala.swing.event.EditDone
import _root_.scala.xml.{NodeSeq, Node}
import filter.TagUsers
import java.awt.event.{ActionListener, ActionEvent, KeyEvent}
import java.awt.{Toolkit, Dimension, Font}
import java.util.Comparator
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, AbstractTableModel}
import javax.swing.{JPopupMenu, JToolBar, JTable, JToggleButton, KeyStroke, Icon, JLabel}
import scala.swing._
import scala.swing.GridBagPanel._
import twitter.{FriendsFollowersDataProvider}
import util.{TableUtil, DesktopUtil}

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

/**
 * Displays a list of friends or followers
 */
class PeoplePane(session: Session, apiHandlers: ApiHandlers, tableModel: UsersTableModel, 
    friends: List[Node], followers: List[Node]) extends GridBagPanel {
  var table: JTable = _
  val tableScrollPane = new ScrollPane {
    table = new PeopleTable(tableModel)
    peer.setViewportView(table)
  }
  val ap = new ActionPrep(table)
  buildActions(ap, table)
  table.addMouseListener(new PopupListener(table, getPopupMenu(ap)))
  var followingButton: JToggleButton = _
  var followersButton: JToggleButton = _
  val searchText = new TextField { val s=new Dimension(100,20); minimumSize = s; preferredSize = s}
  listenTo(searchText)
  reactions += {
    case EditDone(`searchText`) => buildModelData
  }
  val toolbar = new JToolBar {
    setFloatable(false)
    class FriendFollowButton(label: String) extends JToggleButton(label) {
      setSelected(true)
      addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) = buildModelData
      })
    }
    followingButton = new FriendFollowButton("Following: " + friends.size)
    followersButton = new FriendFollowButton("Followers: " + followers.size) 
    add(followingButton)
    add(followersButton)
    add(new JLabel(" Overlap: " + (friends.size + followers.size - tableModel.usersModel.users.size)))
    addSeparator
    add(new JLabel("Search user name: "))
    add(searchText.peer) 
  }
  peer.add(toolbar, new Constraints { grid=(0,0); anchor=Anchor.West }.peer)
  
  add(tableScrollPane, new Constraints { 
    grid=(0,1); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1 
  })
  
  private def buildModelData = tableModel.buildModelData(UserSelection(
    followingButton.isSelected, followersButton.isSelected, searchText.text.length match {
      case 0 => None
      case _ => Some(searchText.text)
    }))

  private def buildActions(ap: ActionPrep, comp: java.awt.Component) = {
    ap.add(Action("View in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap.add(new NextTAction(comp))
    ap.add(new PrevTAction(comp))
    ap.add(Action("Reply") { reply }, Actions.ks(KeyEvent.VK_R))
    val mask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
    ap.add(Action("Follow") { follow }, KeyStroke.getKeyStroke(KeyEvent.VK_F, mask))
    ap.add(Action("Unfollow") { unfollow }, KeyStroke.getKeyStroke(KeyEvent.VK_U, mask))
  }

  private def getPopupMenu(ap: ActionPrep): JPopupMenu = {
    val menu = new JPopupMenu
    for (action <- ap.actions.reverse) 
      menu.add(new MenuItem(action).peer)
    menu
  }
  
  private def getSelectedUsers = TableUtil.getSelectedModelIndexes(table).map(tableModel.usersModel.users(_))
  
  def getSelectedScreenNames: List[String] = {
    getSelectedUsers.map(user => (user \ "screen_name").text)
  }

  private def follow = getSelectedScreenNames foreach apiHandlers.follower.follow
  private def unfollow = getSelectedScreenNames foreach apiHandlers.follower.unfollow
  
  private def viewSelected {
    getSelectedUsers.foreach(user => {
      var uri = "http://twitter.com/" + (user \ "screen_name").text
      DesktopUtil.browse(uri)
    })
  }
  
  private def reply {
    val names = getSelectedUsers.map(user => ("@" + (user \ "screen_name").text)).mkString(" ")
    val sm = new SendMsgDialog(session, null, apiHandlers.sender, Some(names), None)
    sm.visible = true
  }

}
