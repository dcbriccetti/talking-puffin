package org.talkingpuffin.ui

import _root_.scala.swing.event.EditDone
import java.awt.Dimension
import javax.swing.KeyStroke.{getKeyStroke => ks}
import javax.swing.{JButton, JTable, JToolBar, JToggleButton, JLabel}
import scala.swing.GridBagPanel._
import swing.{Reactor, GridBagPanel, ScrollPane, TextField, Action}
import java.awt.event.{KeyEvent, ActionListener, ActionEvent}
import org.talkingpuffin.util.{Loggable, PopupListener}
import org.talkingpuffin.Session
import util.{DesktopUtil, TableUtil}
import org.talkingpuffin.twitter.{TwitterStatus, TwitterUser}

object UserColumns {
  val ARROWS = 0
  val PICTURE = 1
  val SCREEN_NAME = 2
  val NAME = 3
  val FRIENDS = 4
  val FOLLOWERS = 5
  val TAGS = 6
  val LOCATION = 7
  val DESCRIPTION = 8
  val STATUS = 9
  val STATUS_DATE = 10
  val Count = 11
}

/**
 * Displays a list of friends or followers
 */
class PeoplePane(val session: Session, tableModel: UsersTableModel, rels: Relationships, 
    updateCallback: Option[() => Unit]) extends GridBagPanel 
    with Loggable with Reactor {
  var table: JTable = _
  val tableScrollPane = new ScrollPane {
    table = new PeopleTable(tableModel)
    peer.setViewportView(table)
  }
  private val userActions = new UserActions(session, rels)
  val mh = new PopupMenuHelper(table)
  private var specialMenuItems = new SpecialMenuItems(table, tableModel.relationships,
    {getSelectedUsers map(_.id)}, getSelectedScreenNames, {false})
  buildActions(mh, table)
  table.addMouseListener(new PopupListener(table, mh.menu))

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
  val findPeopleText = new TextField { val s=new Dimension(100,20); minimumSize = s; preferredSize = s}
  reactions += {
    case EditDone(`searchText`) => buildModelData
    case EditDone(`findPeopleText`) => findPeople
  }
  listenTo(searchText)
  listenTo(findPeopleText)

  val toolbar = new JToolBar {
    setFloatable(false)
    setLabels
    add(followingButton)
    add(followersButton)
    add(overlapLabel)

    addSeparator

    if (updateCallback.isDefined) {
      add(new JButton(new Action("Reload") {
        toolTip = "Reloads this data from Twitter"
        def apply = updateCallback.get()
      }.peer))
    }

    addSeparator

    add(new JLabel("Search user name: "))
    add(searchText.peer)

    addSeparator

    add(new JLabel("Find people on Twitter: "))
    add(findPeopleText.peer)
  }
  peer.add(toolbar, new Constraints { grid=(0,0); anchor=Anchor.West }.peer)
  
  add(tableScrollPane, new Constraints { 
    grid=(0,1); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1 
  })
  
  session.tweetDetailPanel.connectToTable(table, None)

  reactions += {
    case e: UsersChanged => setLabels
  }
  listenTo(rels)
  
  private def setLabels {
    followingButton.setText("Following: " + rels.friends.length)
    followersButton.setText("Followers: " + rels.followers.length)
    overlapLabel.setText(" Overlap: " + (rels.friends intersect rels.followers).length)
  }
  
  private def buildModelData = tableModel.buildModelData(UserSelection(
    followingButton.isSelected, followersButton.isSelected, searchText.text.length match {
      case 0 => None
      case _ => Some(searchText.text)
    }))
  
  private def findPeople: Unit = {
    val people = session.twitterSession.findPeople(findPeopleText.text)
    debug("Found people: " + people)
    session.windows.peoplePaneCreator.createPeoplePane(findPeopleText.text, 
      None, Some(people), None, None)
  }

  private def buildActions(mh: PopupMenuHelper, comp: java.awt.Component) = {
    mh.add(Action("View in Browser") {viewSelected}, ks(KeyEvent.VK_V,0))
    mh.add(new NextTAction(comp))
    mh.add(new PrevTAction(comp))
    mh add(new TagAction(table, tableModel), ks(KeyEvent.VK_T,0))
    mh.add(Action("Reply") { reply }, ks(KeyEvent.VK_R,0))
    userActions.addCommonItems(mh, specialMenuItems, table, 
        session.tweetDetailPanel.showBigPicture, getSelectedScreenNames)
  }

  private def getSelectedUsers:List[TwitterUser] = 
    TableUtil.getSelectedModelIndexes(table).map(tableModel.usersModel.users(_))
  
  def getSelectedScreenNames: List[String] = getSelectedUsers.map(user => user.screenName)

  private def viewSelected = getSelectedUsers.foreach(u => DesktopUtil.browse("http://twitter.com/" + 
      u.screenName))
  
  private def reply {
    val names = getSelectedUsers.map(user => ("@" + user.screenName)).mkString(" ")
    val sm = new SendMsgDialog(session, null, Some(names), None, None, false)
    sm.visible = true
  }

}
