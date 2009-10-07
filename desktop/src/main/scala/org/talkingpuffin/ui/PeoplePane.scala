package org.talkingpuffin.ui

import _root_.scala.swing.event.EditDone
import java.awt.event.{ActionListener, ActionEvent, KeyEvent}
import java.awt.{Toolkit, Dimension}
import javax.swing.{JButton, JPopupMenu, JTable, JToolBar, JToggleButton, JLabel}
import scala.swing.GridBagPanel._
import swing.{Reactor, MenuItem, GridBagPanel, ScrollPane, TextField, Action}
import org.talkingpuffin.util.{Loggable, PopupListener}
import org.talkingpuffin.ui.util.{TableUtil, DesktopUtil}
import org.talkingpuffin.twitter.{TwitterUser}
import org.talkingpuffin.Session

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
class PeoplePane(session: Session, tableModel: UsersTableModel, rels: Relationships, 
    updateCallback: () => Unit) extends GridBagPanel with Loggable with Reactor {
  var table: JTable = _
  val tableScrollPane = new ScrollPane {
    table = new PeopleTable(tableModel)
    peer.setViewportView(table)
  }
  private val userActions = new UserActions(session.twitterSession, rels)
  val ap = new ActionPrep(table)
  buildActions(ap, table)
  table.addMouseListener(new PopupListener(table, getPopupMenu(ap)))

  class FriendFollowButton(label: String) extends JToggleButton(label) {
    setSelected(true)
    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) = buildModelData
    })
  }

  var followingButton = new FriendFollowButton("")
  var followersButton = new FriendFollowButton("")
  var overlapLabel = new JLabel("")
  val searchText = new TextField { val s=new Dimension(100,20); minimumSize = s; preferredSize = s}
  reactions += {
    case EditDone(`searchText`) => buildModelData
  }
  listenTo(searchText)

  val toolbar = new JToolBar {
    setFloatable(false)
    setLabels
    add(followingButton)
    add(followersButton)
    add(overlapLabel)

    addSeparator

    val reloadAction = new Action("Reload") {
      toolTip = "Reloads this data from Twitter"
      def apply = updateCallback()
    }
    add(new JButton(reloadAction.peer))

    addSeparator

    add(new JLabel("Search user name: "))
    add(searchText.peer) 
  }
  peer.add(toolbar, new Constraints { grid=(0,0); anchor=Anchor.West }.peer)
  
  add(tableScrollPane, new Constraints { 
    grid=(0,1); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1 
  })
  
  reactions += {
    case e: UsersChanged => setLabels
  }
  listenTo(rels)
  
  private def setLabels {
    followingButton.setText("Following: " + rels.friends.length)
    followersButton.setText("Followers: " + rels.followers.length)
    overlapLabel.setText(" Overlap: " + (rels.friends.length + rels.followers.length - tableModel.usersModel.users.length))
  }
  
  private def buildModelData = tableModel.buildModelData(UserSelection(
    followingButton.isSelected, followersButton.isSelected, searchText.text.length match {
      case 0 => None
      case _ => Some(searchText.text)
    }))

  private def buildActions(ap: ActionPrep, comp: java.awt.Component) = {
    ap.add(Action("View in Browser") {viewSelected}, Actions.ks(KeyEvent.VK_V))
    ap.add(new NextTAction(comp))
    ap.add(new PrevTAction(comp))
    ap add(new TagAction(table, tableModel), Actions.ks(KeyEvent.VK_T))
    ap.add(Action("Reply") { reply }, Actions.ks(KeyEvent.VK_R))
    ap.add(Action("Follow"  ) { userActions.follow(getSelectedScreenNames  ) }, UserActions.FollowAccel)
    ap.add(Action("Unfollow") { userActions.unfollow(getSelectedScreenNames) }, UserActions.UnfollowAccel)
    ap.add(Action("Block"   ) { userActions.block(getSelectedScreenNames   ) }, UserActions.BlockAccel)
  }

  private def getPopupMenu(ap: ActionPrep): JPopupMenu = {
    val menu = new JPopupMenu
    for (action <- ap.actions.reverse) 
      menu.add(new MenuItem(action).peer)
    menu
  }
  
  private def getSelectedUsers:List[TwitterUser] = 
    TableUtil.getSelectedModelIndexes(table).map(tableModel.usersModel.users(_))
  
  def getSelectedScreenNames: List[String] = {
    getSelectedUsers.map(user => user.screenName)
  }

  private def viewSelected {
    getSelectedUsers.foreach(user => {
      var uri = "http://twitter.com/" + user.screenName
      DesktopUtil.browse(uri)
    })
  }
  
  private def reply {
    val names = getSelectedUsers.map(user => ("@" + user.screenName)).mkString(" ")
    val sm = new SendMsgDialog(session, null, Some(names), None, None, false)
    sm.visible = true
  }

}
