package org.talkingpuffin.ui

import _root_.scala.swing.{TabbedPane, Component, Reactor}
import filter.{FilterSet, TextFilter, TagUsers}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import state.{PreferencesFactory}
import talkingpuffin.util.Loggable
import twitter._

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val twitterSession: AuthenticatedSession, 
    session: Session, val tagUsers: TagUsers) 
    extends Reactor with ViewCreator with Loggable {
  val prefs = PreferencesFactory.prefsForUser(service, twitterSession.user)
  val providers = new DataProviders(twitterSession, prefs, session.progress)
  val usersTableModel = new UsersTableModel(tagUsers, List[TwitterUser](), List[TwitterUser]())
  
  var views = List[View]()
  
  var followerIds = List[Long]()
  var friendIds = List[Long]()
  var friends = List[TwitterUser]()
  
  reactions += {
    case TableContentsChanged(model, filtered, total) => 
      views.find(_.model == model) match {
        case Some(view) => setTitleInParent(view.pane.peer, view.title + " (" + 
            (if (total == filtered) total else filtered + "/" + total) + ")")
        case _ => info("No view found for model " + model) 
      }
  }

  providers.providers.foreach(provider => {
    createView(provider, None)
    provider.loadNewData
  })
  
  private def setTitleInParent(pane: JComponent, title: String) =
    session.windows.tabbedPane.peer.indexOfComponent(pane) match {
      case -1 => 
        SwingUtilities.getAncestorOfClass(classOf[JFrame], pane) match {
          case null =>
          case jf => jf.asInstanceOf[JFrame].setTitle(title) 
        }
      case i => session.windows.tabbedPane.peer.setTitleAt(i, title)
    }

  def createView[T](dataProvider: DataProvider[T], include: Option[String]): View = {
    val view = View.create(dataProvider, usersTableModel.usersModel, service, twitterSession.user, tagUsers, 
      session, include, this, followerIds, friendIds)
    listenTo(view.model)
    views ::= view
    view
  }

  def componentTitle(comp: Component) = views.filter(s => s.pane == comp)(0).title
  
  def setFollowerIds(followerIds: List[Long]) {
    this.followerIds = followerIds
    views.foreach(_.model.followerIds = followerIds)
  }
  
  def setFriendIds(friendIds: List[Long]) {
    this.friendIds = friendIds
    views.foreach(_.model.friendIds = friendIds)
  }
  
  def setFriends(friends: List[TwitterUser]) {
    this.friends = friends
    val usernames = friends map(_.screenName)
    views.foreach(_.model.friendUsernames = usernames)
  }
}
