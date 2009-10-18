package org.talkingpuffin.ui

import _root_.scala.swing.event.{WindowClosing}
import java.awt.{Dimension}
import javax.swing.{ImageIcon}
import swing.TabbedPane.Page
import swing.{Reactor, Frame, TabbedPane, Label, GridBagPanel}
import org.talkingpuffin.filter.TagUsers
import util.FetchRequest
import org.talkingpuffin.{Main, Globals, Session}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.state.{StateSaver}
import java.text.NumberFormat
import org.talkingpuffin.twitter.{RateLimitStatusEvent, TwitterUser, AuthenticatedSession}

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(service: String, twitterSession: AuthenticatedSession) extends Frame with Loggable 
    with PeoplePaneCreator with Reactor {
  val tagUsers = new TagUsers(service, twitterSession.user)
  TopFrames.addFrame(this)
  val session = new Session(service, twitterSession)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  val tabbedPane = new TabbedPane() {
    preferredSize = new Dimension(900, 600)
  }
  session.windows.tabbedPane = tabbedPane
  session.windows.peoplePaneCreator = this
  private var peoplePane: PeoplePane = _

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  setUpUserStatusReactor
  
  val rels = new Relationships()
  
  val streams = new Streams(service, twitterSession, session, tagUsers, rels)
  session.windows.streams = streams
  mainToolBar.init(streams)
    
  title = Main.title + " - " + service + " " + twitterSession.user
  menuBar = new MainMenuBar(streams.providers, tagUsers)
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
    deafTo(twitterSession.httpPublisher)
    Globals.sessions -= session
    dispose
    StateSaver.save(streams, session.userPrefs, tagUsers)
    TopFrames.removeFrame(this)
  }

  type Users = List[TwitterUser]
  
  def updatePeople = rels.getUsers(twitterSession, mainToolBar)
  
  private def createPeoplePane: Unit = {
    updatePeople
    peoplePane = createPeoplePane("People You Follow and People Who Follow You", "People", None, 
        Some(updatePeople _), false)
  }
  
  def createPeoplePane(longTitle: String, shortTitle: String, users: Option[List[TwitterUser]], 
        updatePeople: Option[() => Unit], selectPane: Boolean): PeoplePane = {
    val model = if (users.isDefined) new UsersTableModel(users, tagUsers, rels) else streams.usersTableModel
    val customRels = if (users.isDefined) {
      new Relationships {
        friends = rels.friends intersect users.get
        friendIds = friends map(_.id)
        followers = rels.followers intersect users.get
        followerIds = followers map(_.id)
      }
    } else rels
    val peoplePane = new PeoplePane(longTitle, shortTitle, session, model, customRels, updatePeople)
    val peoplePage = new Page(shortTitle, peoplePane) {tip = longTitle}
    tabbedPane.pages += peoplePage
    if (selectPane)
      tabbedPane.selection.page = peoplePage
    peoplePane
  }

  private def setUpUserStatusReactor {
    reactions += {
      case e: RateLimitStatusEvent => SwingInvoke.later {
        mainToolBar.remaining.text = NumberFormat.getIntegerInstance.format(e.status.remainingHits)
      }
    }
    listenTo(twitterSession.httpPublisher)
  }

}

  
