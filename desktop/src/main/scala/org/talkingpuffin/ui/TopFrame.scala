package org.talkingpuffin.ui
import _root_.scala.swing.event.{ButtonClicked, WindowClosing}
import filter.{TagUsers}
import java.awt.{Dimension}
import java.util.concurrent.{Callable, Executors}
import javax.swing._
import scala.swing._
import talkingpuffin.util.Loggable
import twitter._
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
    val highFol = streams.followingProvider.getHighestId
    val highMen = streams.mentionsProvider.getHighestId
    info("Saving last seen IDs for " + twitterSession.user + ". Following: " + highFol + ", mentions: " + highMen)
    val prefs = session.userPrefs
    if (highFol.isDefined) prefs.put("highestId"       , highFol.get.toString())
    if (highMen.isDefined) prefs.put("highestMentionId", highMen.get.toString())
    tagUsers.save
    streams.views.last.pane.saveState // TODO instead save the order of the last status pane changed
  }
  
  private def getUserIds {
    /** Background user fetcher */
    def fetchUsersBg(getter: Int => List[TwitterUserId], setter: List[String] => Unit) {
      new SwingWorker[List[TwitterUserId], Object] {
        def doInBackground = twitterSession.loadAll(getter)
        override def done = setter(get.map(_.id.toString))
      }.execute
    }
   
    fetchUsersBg(twitterSession.getFriendsIds  , streams.setFriendIds)
    fetchUsersBg(twitterSession.getFollowersIds, streams.setFollowerIds)
  }
  
  private def createPeoplePane: Unit = {
    mainToolBar.startOperation

    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFriends)
    })
    val followersFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFollowers)
    })

    new SwingWorker[Tuple2[List[TwitterUser],List[TwitterUser]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)

      override def done = {
        val (friends, followers) = get 
              
        streams.usersTableModel.friends = friends
        streams.usersTableModel.followers = followers
        streams.usersTableModel.usersChanged
        streams.setFriends(friends)
 
        val paneTitle = "People (" + friends.length + ", " + followers.length + ")"
        val pane = new PeoplePane(session, streams.usersTableModel, friends, followers)
        tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
        mainToolBar.stopOperation
      }
    }.execute
  }
}
  
