package org.talkingpuffin.ui

import java.awt.{Rectangle}
import java.text.NumberFormat
import scala.swing.event.{WindowClosing}
import swing.{Reactor, Frame, Label, GridBagPanel}
import org.talkingpuffin.{Main, Globals, Session, Constants}
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.twitter.{RateLimitStatusEvent, TwitterUser, AuthenticatedSession}
import org.talkingpuffin.util.{FetchRequest, Loggable}
import org.talkingpuffin.state.{GlobalPrefs, StateSaver}
import util.{ColTiler, AppEvent, eventDistributor}
import javax.swing.{JInternalFrame, ImageIcon}

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(service: String, twitterSession: AuthenticatedSession) extends Frame with Loggable 
    with PeoplePaneCreator with Reactor {
  val prefs = GlobalPrefs.prefsForUser(service, twitterSession.user)
  val tagUsers = new TagUsers(service, twitterSession.user)
  TopFrames.addFrame(this)
  val session = new Session(service, twitterSession)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  session.windows.peoplePaneCreator = this
  private var peoplePane: PeoplePane = _

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  setUpUserStatusReactor
  
  val rels = new Relationships()
  
  val providers = new DataProviders(twitterSession, prefs, session.progress)
  session.dataProviders = providers
  val streams = new Streams(service, prefs, session, tagUsers, rels)
  session.windows.streams = streams
  menuBar = new MainMenuBar(session, tagUsers)
  mainToolBar.init(streams)
    
  title = Main.title + " - " + service + " " + twitterSession.user
  reactions += {
    case e: AppEvent if e.session != session =>  // Ignore all from other sessions 
    case e: NewFollowingViewEvent => createView(providers.following, e.include, None) 
    case e: NewViewEvent => createView(e.provider, e.include, None) 
    case e: NewPeoplePaneEvent => createPeoplePane 
    case e: TileViewsEvent => tileViews(e.numRows) 
    case e: SendStatusEvent => (new SendMsgDialog(session, null, None, None, None, false)).visible = true 
    case e: SendDirectMessageEvent => (new SendMsgDialog(session, null, None, None, None, true)).visible = true
  }
  listenTo(eventDistributor)

  contents = new GridBagPanel {
    val userPic = new Label
    val picFetcher = new PictureFetcher("Frame picture " + hashCode, None)
    picFetcher.requestItem(new FetchRequest(twitterSession.getUserDetail().profileImageURL, null, 
      (imageReady: PictureFetcher.ImageReady) => {
        if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
          userPic.icon = imageReady.resource.image 
        }
      }))
    add(userPic, new Constraints { grid = (0,0); gridheight=2})
    add(session.statusMsgLabel, new Constraints {
      grid = (1,0); anchor=GridBagPanel.Anchor.West; fill = GridBagPanel.Fill.Horizontal; weightx = 1;  
      })
    peer.add(mainToolBar, new Constraints {grid = (1,1); anchor=GridBagPanel.Anchor.West}.peer)
    peer.add(session.desktopPane, new Constraints {grid = (0,2); gridwidth=2; 
      fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1}.peer)
  }

  reactions += {
    case WindowClosing(_) => close
  }

  peer.setLocationRelativeTo(null)
  listenTo(rels)
  reactions += {
    case ic: IdsChanged => 
      if ((rels.followers.isEmpty && rels.friends.isEmpty) && 
          (rels.followerIds.length + rels.friendIds.length < Constants.MaxPeopleForAutoPaneCreation)) {
        updatePeople
      }
  }
  rels.getIds(twitterSession, mainToolBar)
  
  pack
  visible = true
  setFocus
  streams.views(0).frame.get.setSelected(true)

  def setFocus = streams.views.last.pane.requestFocusForTable
  
  def close {
    streams.stop
    deafTo(twitterSession.httpPublisher)
    Globals.sessions -= session
    dispose
    StateSaver.save(streams, session.userPrefs, tagUsers)
    TopFrames.removeFrame(this)
  }

  type Users = List[TwitterUser]
  
  def createPeoplePane(longTitle: String, otherRels: Option[Relationships], users: Option[Users], 
        updatePeople: Option[() => Unit], location: Option[Rectangle]): PeoplePane = {
    def getRels = if (otherRels.isDefined) otherRels.get else rels
    val model = 
      if (users.isDefined || otherRels.isDefined) 
        new UsersTableModel(users, tagUsers, getRels) 
      else 
        streams.usersTableModel
    val customRels = if (users.isDefined) {
      new Relationships {
        friends = rels.friends intersect users.get
        friendIds = friends map(_.id)
        followers = rels.followers intersect users.get
        followerIds = followers map(_.id)
      }
    } else getRels
    val peoplePane = new PeoplePane(session, model, customRels, updatePeople)
    session.desktopPane.add(
      new JInternalFrame(longTitle, true, true, true, true) {
        setLayer(3)
        setContentPane(peoplePane.peer)
        pack()
        setVisible(true)
      })
    peoplePane
  }

  private def updatePeople = {
    rels.getUsers(twitterSession, twitterSession.user, mainToolBar)
  }
          
  private def createPeoplePane: Unit = {
    peoplePane = createPeoplePane("People You Follow and People Who Follow You", None, None, 
        Some(updatePeople _), None)
  }
  
  private def setUpUserStatusReactor {
    reactions += {
      case e: RateLimitStatusEvent => SwingInvoke.later {
        mainToolBar.remaining.text = NumberFormat.getIntegerInstance.format(e.status.remainingHits)
      }
    }
    listenTo(twitterSession.httpPublisher)
  }

  private def tileViews(numRows: Int) {
    val frames = (for {
      v <- session.windows.streams.views
      if v.frame.isDefined && ! v.frame.get.isIcon
    } yield v.frame.get) sort(_.getLocation().x < _.getLocation().x)
    if (frames != Nil) {
      val tiler = new ColTiler(session.desktopPane.getSize, frames.length, numRows)
      frames.foreach(_.setBounds(tiler.next))
    }
  }

  private def createView(provider: DataProvider, include: Option[String], location: Option[Rectangle]) {
    streams.createView(session.desktopPane, provider, include, location)
    provider.loadContinually()
  }
}

  
