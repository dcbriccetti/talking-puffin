package org.talkingpuffin.ui

import java.awt.{Rectangle}
import javax.swing.ImageIcon
import java.text.NumberFormat
import scala.swing.event.{WindowClosing}
import org.talkingpuffin.{Main, Globals, Session, Constants}
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.util.{FetchRequest, Loggable}
import org.talkingpuffin.state.{GlobalPrefs, StateSaver}
import util.{AppEvent, eventDistributor}
import twitter4j.{RateLimitStatusListener, Twitter, User, RateLimitStatusEvent}
import swing._
import swing.TabbedPane.Page

/**
 * The top-level application Swing frame window. There is one per user session.
 */
class TopFrame(tw: Twitter) extends Frame with Loggable
    with PeoplePaneCreator with Reactor {
  val service = org.talkingpuffin.twitter.Constants.ServiceName
  val prefs = GlobalPrefs.prefsForUser(service, tw.getScreenName)
  val tagUsers = new TagUsers(service, tw.getScreenName)
  TopFrames.addFrame(this)
  val session = new Session(service, tw)
  Globals.sessions ::= session
  iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
  session.peoplePaneCreator = this
  private var peoplePane: PeoplePane = _

  val mainToolBar = new MainToolBar
  session.progress = mainToolBar
  setUpUserStatusReactor

  val rels = new Relationships()
  
  val providers = new DataProviders(session, prefs, session.progress)
  session.dataProviders = providers
  val streams = new Streams(prefs, session, tagUsers, rels)
  session.streams = streams
  menuBar = new MainMenuBar(session, tagUsers)
  mainToolBar.init(streams)
    
  title = Main.title + " - " + service + " " + tw.getScreenName
  reactions += {
    case e: AppEvent if e.session != session =>  // Ignore all from other sessions 
    case e: NewFollowingViewEvent => createView(providers.following, e.include, None) 
    case e: NewViewEvent => createView(e.provider, e.include, None) 
    case e: NewPeoplePaneEvent => createPeoplePane 
    case e: SendStatusEvent => (new SendMsgDialog(session, null, None, None, None, false)).visible = true
    case e: SendDirectMessageEvent => (new SendMsgDialog(session, null, None, None, None, true)).visible = true
  }
  listenTo(eventDistributor)

  contents = new GridBagPanel {
    val userPic = new Label
    val picFetcher = new PictureFetcher("Frame picture " + hashCode, None)
    picFetcher.requestItem(new FetchRequest(tw.showUser(tw.getScreenName).
      getProfileImageURL.toString, null,
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
    add(session.tabbedPane, new Constraints {
        grid = (0,2); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; gridwidth=2})
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
  rels.getIds(session, mainToolBar)
  
  pack
  visible = true
  setFocus

  def setFocus = streams.views.foreach(_.pane.requestFocusForTable)
  
  override def close {
    streams.stop
    tw.setRateLimitStatusListener(null)
    Globals.sessions -= session
    dispose
    StateSaver.save(streams, session.userPrefs, tagUsers)
    TopFrames.removeFrame(this)
  }

  type Users = List[User]
  
  def createPeoplePane(longTitle: String, shortTitle: String, otherRels: Option[Relationships], users: Option[Users],
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
        friendIds = friends map(_.getId.toLong)
        followers = rels.followers intersect users.get
        followerIds = followers map(_.getId.toLong)
      }
    } else getRels
    val peoplePane = new PeoplePane(longTitle, shortTitle, session, model, customRels, updatePeople)
    session.tabbedPane.pages += new Page(shortTitle, peoplePane) {tip = longTitle}
    peoplePane
  }

  private def updatePeople = {
    rels.getUsers(session, tw.getScreenName, mainToolBar)
  }
          
  private def createPeoplePane: Unit = {
    peoplePane = createPeoplePane("People You Follow and People Who Follow You", "People", None, None,
        Some(updatePeople _), None)
  }
  
  private def setUpUserStatusReactor {
    tw.setRateLimitStatusListener(new RateLimitStatusListener() {
      def onRateLimitReached(e: RateLimitStatusEvent) = {}
      def onRateLimitStatus(e: RateLimitStatusEvent) = SwingInvoke.later {
        mainToolBar.remaining.text = NumberFormat.getIntegerInstance.format(e.getRateLimitStatus.getRemainingHits)
      }
    })
  }

  private def createView(provider: DataProvider, include: Option[String], location: Option[Rectangle]) {
    streams.createView(session.tabbedPane, provider, include, location)
    provider.loadContinually()
  }
}

  
