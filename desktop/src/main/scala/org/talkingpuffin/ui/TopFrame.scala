package org.talkingpuffin.ui

import _root_.scala.swing.event.{WindowClosing}
import filter.{TagUsers}
import java.awt.{Dimension}
import javax.swing.{ImageIcon}
import state.PrefKeys
import swing.TabbedPane.Page
import swing.{Reactor, Frame, TabbedPane, Label, GridBagPanel}
import talkingpuffin.util.Loggable
import twitter.{TwitterUser, AuthenticatedSession}
import ui._
import ui.util.FetchRequest

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(service: String, twitterSession: AuthenticatedSession) extends Frame with Loggable with Reactor {
  val tagUsers = new TagUsers(service, twitterSession.user)
  TopFrames.addFrame(this)
  val session = new Session(service, twitterSession)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  val tabbedPane = new TabbedPane() {
    preferredSize = new Dimension(900, 600)
  }
  session.windows.tabbedPane = tabbedPane
  private var peoplePage: Page = _
  private var peoplePane: PeoplePane = _

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  
  val rels = new Relationships()
  reactions += { 
    case _: UsersChanged =>
      if (peoplePage != null) {
        // pane.title_= is buggy. index could be wrong
        val pane = tabbedPane.peer
        pane.setTitleAt(pane.indexOfComponent(peoplePane.peer), 
          peoplePaneTitle(rels.friends.length, rels.followers.length)) 
      }
  }
  listenTo(rels)
  
  val streams = new Streams(service, twitterSession, session, tagUsers, rels)
  session.windows.streams = streams
  mainToolBar.init(streams)
    
  title = Main.title + " - " + service + " " + twitterSession.user
  menuBar = new MainMenuBar(streams.providers)
  reactions += {
    case e: NewViewEvent => streams.createView(e.provider, None)
  }
  listenTo(menuBar)

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
    case WindowClosing(_) => close
  }

  peer.setLocationRelativeTo(null)
  rels.getIds(twitterSession, mainToolBar)
  createPeoplePane

  def setFocus = streams.views.last.pane.requestFocusForTable
  
  def close {
    Globals.sessions -= session
    dispose
    saveState
    TopFrames.removeFrame(this)
  }

  private def saveState {
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
  
  type Users = List[TwitterUser]
  
  def peoplePaneTitle(numFriends: Int, numFollowers: Int) = "People (" + numFriends + ", " + numFollowers + ")"
  
  def updatePeople = rels.getUsers(twitterSession, mainToolBar)
  
  private def createPeoplePane: Unit = {
    updatePeople
    peoplePane = new PeoplePane(session, streams.usersTableModel, rels, updatePeople _)
    peoplePage = new Page(peoplePaneTitle(rels.friends.length, rels.followers.length), peoplePane)
    tabbedPane.pages += peoplePage
  }
}

  
