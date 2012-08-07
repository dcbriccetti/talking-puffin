package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import _root_.scala.swing.event.EditDone
import java.awt.Dimension
import javax.swing.KeyStroke.{getKeyStroke => ks}
import javax.swing.{JButton, JToolBar, JToggleButton, JLabel}
import scala.swing.GridBagPanel._
import swing.{Reactor, GridBagPanel, ScrollPane, TextField, Action}
import java.awt.event.{KeyEvent, ActionListener, ActionEvent}
import org.talkingpuffin.util.{Loggable, PopupListener}
import org.talkingpuffin.Session
import org.talkingpuffin.apix.RichUser._
import util.{Dockable, DesktopUtil, TableUtil}
import twitter4j.{Status, User}
import org.talkingpuffin.model.UserSelection

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
class PeoplePane(val longTitle: String, val shortTitle: String, val session: Session,
    tableModel: UsersTableModel, rels: Relationships,
    updateCallback: Option[() => Unit]) extends GridBagPanel 
    with Loggable with Reactor with Dockable {
  var table: PeopleTable = _
  val tableScrollPane = new ScrollPane {
    table = new PeopleTable(tableModel)
    peer.setViewportView(table)
  }
  private val tweetDetailPanel = new TweetDetailPanel(session, None)
  private val userActions = new UserActions(session, rels)
  val mh = new PopupMenuHelper(table)
  private val specialMenuItems = new SpecialMenuItems(table, tableModel.relationships, {
    getSelectedUsers map (_.getId.toLong)
  }, getSelectedScreenNames(retweets = true), {
    false
  })
  buildActions(mh, table)
  table.addMouseListener(new PopupListener(table, mh.menu))

  private class FilterButton(label: String) extends JToggleButton(label) {
    setSelected(true)
    addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        buildModelData()
      }
    })
  }

  private val followingButton = new FilterButton("")
  private val followersButton = new FilterButton("")
  private val overlapLabel = new JLabel("")
  private val emptyDescriptionsButton = new FilterButton("No Desc.") {
    tooltip = "Show or hide people with no description"
  }
  private val searchText = new TextField { val s=new Dimension(100,20); minimumSize = s; preferredSize = s}
  private val findPeopleText = new TextField { val s=new Dimension(100,20); minimumSize = s; preferredSize = s}
  reactions += {
    case EditDone(`searchText`) => buildModelData()
    case EditDone(`findPeopleText`) => findPeople()
  }
  listenTo(searchText)
  listenTo(findPeopleText)

  val toolbar = new JToolBar {
    setFloatable(false)
    setLabels()
    add(followingButton)
    add(followersButton)
    add(overlapLabel)
    add(emptyDescriptionsButton)

    addSeparator()

    if (updateCallback.isDefined) {
      add(new JButton(new Action("Reload") {
        toolTip = "Reloads this data from Twitter"
        def apply() {
          updateCallback.get()
        }
      }.peer))
    }

    addSeparator()

    add(new JLabel("Search user name: "))
    add(searchText.peer)

    addSeparator()

    add(new JLabel("Find people on Twitter: "))
    add(findPeopleText.peer)

    addSeparator()
    add(dockedButton)
    add((new CommonToolbarButtons).createDetailsButton(tweetDetailPanel))
  }
  peer.add(toolbar, new Constraints { grid=(0,0); anchor=Anchor.West }.peer)
  
  add(tableScrollPane, new Constraints { 
    grid=(0,1); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1 
  })
  
  add(tweetDetailPanel, new Constraints{
    grid = (0,2); fill = GridBagPanel.Fill.Horizontal
  })

  tweetDetailPanel.connectToTable(table, None)

  reactions += {
    case e: UsersChanged => setLabels()
  }
  listenTo(rels)
  
  private def setLabels() {
    followingButton.setText("Following: " + rels.friends.length)
    followersButton.setText("Followers: " + rels.followers.length)
    overlapLabel.setText(" Overlap: " + (rels.friends intersect rels.followers).length)
  }
  
  private def buildModelData() {
    tableModel.buildModelData(UserSelection(
      followingButton.isSelected, followersButton.isSelected, emptyDescriptionsButton.isSelected,
        if (searchText.text.isEmpty) None else Some(searchText.text)
      ))
  }
  
  private def findPeople() {
    val people = session.twitter.searchUsers(findPeopleText.text, 1).toList
    debug("Found people: " + people)
    session.peoplePaneCreator.createPeoplePane(findPeopleText.text, findPeopleText.text,
      None, Some(people), None, None)
  }

  private def buildActions(mh: PopupMenuHelper, comp: java.awt.Component) {
    mh.add(Action("View in Browser") {viewSelected()}, ks(KeyEvent.VK_V,0))
    mh.add(new NextTAction(comp))
    mh.add(new PrevTAction(comp))
    mh.add(Action("Reply") { reply() }, ks(KeyEvent.VK_R,0))
    userActions.addCommonItems(mh, specialMenuItems, table, 
      tweetDetailPanel.showBigPicture(), getSelectedScreenNames, getSelectedStatuses)
  }

  private def getSelectedUsers:List[User] =
    TableUtil.getSelectedModelIndexes(table).map(tableModel.usersModel.users(_))
  
  def getSelectedScreenNames(retweets: Boolean): List[String] = getSelectedUsers.map(_.getScreenName)

  def getSelectedStatuses(retweets: Boolean): List[Status] = for {
    user <- getSelectedUsers
    status <- user.status
  } yield status

  private def viewSelected() {
    getSelectedUsers.foreach(u => DesktopUtil.browse("http://twitter.com/" +
      u.getScreenName))
  }
  
  private def reply() {
    val names = getSelectedUsers.map(user => ("@" + user.getScreenName)).mkString(" ")
    val sm = new SendMsgDialog(session, null, Some(names), None, None, false)
    sm.visible = true
  }
}
