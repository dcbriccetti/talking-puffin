package org.talkingpuffin.ui

import _root_.scala.swing.event.{WindowClosing}
import filter.{TagUsers}
import java.awt.{Dimension}
import javax.swing.{SwingWorker, ImageIcon}
import state.PrefKeys
import swing.TabbedPane.Page
import swing.{Frame, TabbedPane, Label, GridBagPanel}
import talkingpuffin.util.Loggable
import twitter.{TwitterUserId, TwitterUser, AuthenticatedSession}
import ui._
import ui.util.FetchRequest

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(service: String, twitterSession: AuthenticatedSession) extends Frame with Loggable {
  val tagUsers = new TagUsers(service, twitterSession.user)
  TopFrames.addFrame(this)
  val session = new Session(twitterSession)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  val tabbedPane = new TabbedPane() {
    preferredSize = new Dimension(900, 600)
  }
  session.windows.tabbedPane = tabbedPane

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  val streams = new Streams(service, twitterSession, session, tagUsers)
  session.windows.streams = streams
  mainToolBar.init(streams)
    
  title = Main.title + " - " + service + " " + twitterSession.user
  menuBar = new MainMenuBar

  contents = new GridBagPanel {
    val userPic = new Label
    val picFetcher = new PictureFetcher(None, (imageReady: PictureFetcher.ImageReady) => {
      if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
        userPic.icon = imageReady.resource.image 
      }
    })
    picFetcher.requestItem(new FetchRequest(twitterSession.getUserDetail().profileImageURL, null))
    add(userPic, new Constraints { grid = (0,0); gridheight=2})
    add(session.status, new Constraints {
      grid = (1,0); anchor=GridBagPanel.Anchor.West; fill = GridBagPanel.Fill.Horizontal; weightx = 1;  
      })
    peer.add(mainToolBar, new Constraints {grid = (1,1); anchor=GridBagPanel.Anchor.West}.peer)
    add(tabbedPane, new Constraints {
      grid = (0,2); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; gridwidth=2})
  }

  reactions += {
    case WindowClosing(_) => {
      Globals.sessions = Globals.sessions remove(s => s == session)
      saveState
      TopFrames.removeFrame(this)
    }
  }

  peer.setLocationRelativeTo(null)
  getUserIds
  createPeoplePane

  def setFocus = streams.views.last.pane.requestFocusForTable
  
  def saveState {
    val highFol = streams.providers.followingProvider.getHighestId
    val highMen = streams.providers.mentionsProvider.getHighestId
    val highDmReceived = streams.providers.dmsReceivedProvider.getHighestId
    val highDmSent = streams.providers.dmsSentProvider.getHighestId
    info("Saving last seen IDs for " + twitterSession.user + ". Following: " + highFol + 
        ", mentions: " + highMen + ", DMs rvcd: " + highDmReceived + ", DMs sent: " + highDmSent)
    val prefs = session.userPrefs
    if (highFol.isDefined) prefs.put(PrefKeys.HIGHEST_ID       , highFol.get.toString())
    if (highMen.isDefined) prefs.put(PrefKeys.HIGHEST_MENTION_ID, highMen.get.toString())
    if (highDmReceived.isDefined ) prefs.put(PrefKeys.HIGHEST_RECEIVED_DM_ID, highDmReceived.get.toString())
    if (highDmSent.isDefined ) prefs.put(PrefKeys.HIGHEST_SENT_DM_ID, highDmSent.get.toString())
    tagUsers.save
    streams.views.last.pane.saveState // TODO instead save the order of the last status pane changed
  }
  
  private def getUserIds {
    /** Background user fetcher */
    def fetchUsersBg(getter: Int => List[TwitterUserId], setter: List[Long] => Unit) {
      new SwingWorker[List[TwitterUserId], Object] {
        def doInBackground = twitterSession.loadAll(getter)
        override def done = setter(get.map(_.id))
      }.execute
    }
   
    fetchUsersBg(twitterSession.getFriendsIds  , streams.setFriendIds)
    fetchUsersBg(twitterSession.getFollowersIds, streams.setFollowerIds)
  }
  
  type Users = List[TwitterUser]
  
  def updatePeople(): Unit = updatePeopleAndCallBack((friends: Users, followers: Users) => {})
  
  private var peoplePage: Page = _
  def peoplePaneTitle(numFriends: Int, numFollowers: Int) = "People (" + numFriends + ", " + numFollowers + ")"
  
  def updatePeopleAndCallBack(edtCallback: (Users, Users) => Unit) = {
    mainToolBar.startOperation

    PeopleProvider.get(twitterSession, (friends: Users, followers: Users) => {
      streams.usersTableModel.friends = friends
      streams.usersTableModel.followers = followers
      streams.usersTableModel.usersChanged()
      streams.setFriends(friends)

      edtCallback(friends, followers)
      if (peoplePage != null) peoplePage.title = peoplePaneTitle(friends.length, followers.length) 
      mainToolBar.stopOperation
    })
    
  }
  
  private def createPeoplePane: Unit = {
    updatePeopleAndCallBack((friends: Users, followers: Users) => {
      val pane = new PeoplePane(session, streams.usersTableModel, friends, followers, updatePeople)
      peoplePage = new Page(peoplePaneTitle(friends.length, followers.length), pane)
      tabbedPane.pages += peoplePage
    })
  }
}

  
