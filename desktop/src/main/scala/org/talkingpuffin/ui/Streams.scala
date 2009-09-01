package org.talkingpuffin.ui

import _root_.scala.swing.{TabbedPane, Component, Reactor}
import filter.{FilterSet, TextFilter, TagUsers}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import state.PreferencesFactory
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
  val followingProvider = new FollowingProvider(user, getHighest("highestId"), session.progress)
  val mentionsProvider  = new MentionsProvider (user, getHighest("highestMentionId"), session.progress)
  val usersTableModel   = new UsersTableModel  (tagUsers, List[TwitterUser](), List[TwitterUser]())
  
  var views = List[View]()
  
  val folTitle = new TitleCreator(followingProvider.providerName)
  val repTitle = new TitleCreator(mentionsProvider.providerName)

  var followerIds = List[String]()
  
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
  
  // Now that views, models and listeners are in place, get data
  followingProvider.loadNewData
  mentionsProvider.loadNewData

  private def setTitleInParent(pane: JComponent, title: String) =
    session.windows.tabbedPane.peer.indexOfComponent(pane) match {
      case -1 => 
        SwingUtilities.getAncestorOfClass(classOf[JFrame], pane) match {
          case null =>
          case jf => jf.asInstanceOf[JFrame].setTitle(title) 
        }
      case i => session.windows.tabbedPane.peer.setTitleAt(i, title)
    }

  private def createView(tweetsProvider: TweetsProvider, title: String, include: Option[String]): View = {
    val fs = new FilterSet(session, user.user, tagUsers)
    if (include.isDefined) {
      fs.includeTextFilters.list ::= new TextFilter(include.get, false) 
    }
    val sto = new StatusTableOptions(true, true, true)
    val isMentions = tweetsProvider.isInstanceOf[MentionsProvider] // TODO do without this test
    val model = if (isMentions) 
      new StatusTableModel(sto, tweetsProvider, usersTableModel, fs, service, user.user, tagUsers) with Mentions
    else 
      new StatusTableModel(sto, tweetsProvider, usersTableModel, fs, service, user.user, tagUsers)
    val pane = new StatusPane(session, title, model, fs, this)
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    listenTo(model)
    val view = new View(title, model, pane)
    view.model.followerIds = followerIds
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
  
  def createRepliesViewFor(include: String) = createView(mentionsProvider, repTitle.create, Some(include))

  def createMentionsView: View = createView(mentionsProvider, repTitle.create, None)
  
  def componentTitle(comp: Component) = views.filter(s => s.pane == comp)(0).title
  
  def setFollowerIds(followerIds: List[String]) {
    this.followerIds = followerIds
    views.foreach(_.model.followerIds = followerIds)
  }
}

