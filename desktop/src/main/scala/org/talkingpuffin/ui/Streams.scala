package org.talkingpuffin.ui

import _root_.scala.swing.{TabbedPane, Component, Reactor}
import filter.{FilterSet, TextFilter, TagUsers}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import state.{PrefKeys, PreferencesFactory}
import talkingpuffin.util.Loggable
import twitter._

case class View(val title: String, val model: StatusTableModel, val pane: StatusPane)

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val user: AuthenticatedSession, session: Session, val tagUsers: TagUsers) 
    extends Reactor with Loggable {
  val prefs = PreferencesFactory.prefsForUser(service, user.user)

  private def getHighest(key: String) = 
    prefs.get(key, null) match {case null => None; case v => Some(java.lang.Long.parseLong(v))} 
  val followingProvider = new FollowingProvider(user, getHighest(PrefKeys.HIGHEST_ID), session.progress)
  val mentionsProvider  = new MentionsProvider (user, getHighest(PrefKeys.HIGHEST_MENTION_ID), session.progress)
  val dmsReceivedProvider = new DmsReceivedProvider (user, getHighest(PrefKeys.HIGHEST_RECEIVED_DM_ID), session.progress)
  val dmsSentProvider = new DmsSentProvider (user, getHighest(PrefKeys.HIGHEST_SENT_DM_ID), session.progress)
  val usersTableModel   = new UsersTableModel  (tagUsers, List[TwitterUser](), List[TwitterUser]())
  
  var views = List[View]()
  
  var followerIds = List[String]()
  var friendIds = List[String]()
  var friends = List[TwitterUser]()
  
  private val folTitle = new TitleCreator(followingProvider.providerName)
  private val repTitle = new TitleCreator(mentionsProvider.providerName)
  private val dmsReceivedTitle = new TitleCreator(dmsReceivedProvider.providerName)
  private val dmsSentTitle = new TitleCreator(dmsSentProvider.providerName)

  reactions += {
    case TableContentsChanged(model, filtered, total) => 
      views.find(_.model == model) match {
        case Some(view) => setTitleInParent(view.pane.peer, view.title + " (" + 
            (if (total == filtered) total else filtered + "/" + total) + ")")
        case _ => info("No view found for model " + model) 
      }
  }

  createFollowingView
  createMentionsView
  createDmsReceivedView
  createDmsSentView
  
  // Now that views, models and listeners are in place, get data
  followingProvider.loadNewData
  mentionsProvider.loadNewData
  dmsReceivedProvider.loadNewData
  dmsSentProvider.loadNewData

  private def setTitleInParent(pane: JComponent, title: String) =
    session.windows.tabbedPane.peer.indexOfComponent(pane) match {
      case -1 => 
        SwingUtilities.getAncestorOfClass(classOf[JFrame], pane) match {
          case null =>
          case jf => jf.asInstanceOf[JFrame].setTitle(title) 
        }
      case i => session.windows.tabbedPane.peer.setTitleAt(i, title)
    }

  private def createView[T](dataProvider: DataProvider[T], title: String, include: Option[String]): View = {
    val fs = new FilterSet(session, user.user, tagUsers)
    if (include.isDefined) {
      fs.includeTextFilters.list ::= new TextFilter(include.get, false) 
    }
    val sto = new StatusTableOptions(true, true, true)
    val model = dataProvider match {
      case p: FollowingProvider => new StatusTableModel(sto, p, usersTableModel,
        fs, service, user.user, tagUsers)
      case p: MentionsProvider => new StatusTableModel(sto, p, usersTableModel,
        fs, service, user.user, tagUsers) with Mentions
      case p: DmsReceivedProvider => new StatusTableModel(sto, p, usersTableModel,
        fs, service, user.user, tagUsers)
      case p: DmsSentProvider => new StatusTableModel(sto, p, usersTableModel,
        fs, service, user.user, tagUsers)
    }
    val pane = new StatusPane(session, title, model, fs, this)
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    listenTo(model)
    val view = new View(title, model, pane)
    view.model.followerIds = followerIds
    view.model.friendIds = friendIds
    views ::= view
    view
  }

  class TitleCreator(baseName: String) {
    var index = 0
    def create: String = {
      index += 1
      if (index == 1) baseName else baseName + index
    }
  }
  
  def createFollowingViewFor(include: String) = createView(followingProvider, folTitle.create, Some(include))

  def createFollowingView: View = createView(followingProvider, folTitle.create, None)
  
  def createMentionsViewFor(include: String) = createView(mentionsProvider, repTitle.create, Some(include))

  def createMentionsView: View = createView(mentionsProvider, repTitle.create, None)
  
  def createDmsReceivedViewFor(include: String) = createView(dmsReceivedProvider, dmsReceivedTitle.create, Some(include))

  def createDmsReceivedView: View = createView(dmsReceivedProvider, dmsReceivedTitle.create, None)
  
  def createDmsSentViewFor(include: String) = createView(dmsSentProvider, dmsSentTitle.create, Some(include))

  def createDmsSentView: View = createView(dmsSentProvider, dmsSentTitle.create, None)
  
  def componentTitle(comp: Component) = views.filter(s => s.pane == comp)(0).title
  
  def setFollowerIds(followerIds: List[String]) {
    this.followerIds = followerIds
    views.foreach(_.model.followerIds = followerIds)
  }
  
  def setFriendIds(friendIds: List[String]) {
    this.friendIds = friendIds
    views.foreach(_.model.friendIds = friendIds)
  }
  
  def setFriends(friends: List[TwitterUser]) {
    this.friends = friends
    val usernames = friends map(_.screenName)
    views.foreach(_.model.friendUsernames = usernames)
  }
}

