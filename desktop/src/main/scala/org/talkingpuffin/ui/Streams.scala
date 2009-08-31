package org.talkingpuffin.ui

import _root_.scala.swing.{TabbedPane, Component, Reactor}
import _root_.scala.xml.Node
import filter.{FilterSet, TextFilter, TagUsers}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import state.PreferencesFactory
import twitter.{AuthenticatedSession, TweetsProvider, MentionsProvider, TwitterUser}

case class View(val title: String, val model: StatusTableModel, val pane: StatusPane)

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val user: AuthenticatedSession, session: Session, val tagUsers: TagUsers) 
    extends Reactor {
  val prefs = PreferencesFactory.prefsForUser(service, user.user)
  
  val tweetsProvider = new TweetsProvider(user,
    prefs.get("highestId", null) match {case null => None; case v => Some(java.lang.Long.parseLong(v))}, 
    "Following", session.progress)
  val mentionsProvider = new MentionsProvider(user,
    prefs.get("highestMentionId", null) match {case null => None; case v => Some(java.lang.Long.parseLong(v))},
    session.progress)
  val usersTableModel = new UsersTableModel(tagUsers, List[TwitterUser](), List[TwitterUser]())
  
  var views = List[View]()
  
  val folTitle = new TitleCreator("Following")
  val repTitle = new TitleCreator("Mentions")

  var followerIds = List[String]()
  
  reactions += {
    case TableContentsChanged(model, filtered, total) => {
      if (views.length == 0) println("streamInfoList is empty. Ignoring table contents changed.")
      else {
        val filteredList = views.filter(_.model == model)
        if (filteredList.length == 0) println("No matches in streamInfoList for model " + model)
        else {
          val si = filteredList(0)
          setTitleInParent(si.pane.peer, createTweetsTitle(si.title, filtered, total))
        }
      }
    }
  }

  createFollowingView
  createRepliesView
  
  // Now that views, models and listeners are in place, get data
  tweetsProvider.loadNewData
  mentionsProvider.loadNewData

  private def setTitleInParent(pane: JComponent, title: String) {
    session.windows.tabbedPane.peer.indexOfComponent(pane) match {
      case -1 => {
        SwingUtilities.getAncestorOfClass(classOf[JFrame], pane) match {
          case null =>
          case parent => parent.asInstanceOf[JFrame].setTitle(title)
        }
      }
      case tabbedPaneIndex => session.windows.tabbedPane.peer.setTitleAt(tabbedPaneIndex, title)
    }
  }

  private def createTweetsTitle(paneTitle: String, filtered: Int, total: Int): String = 
    paneTitle + " (" + (total - filtered match {
      case 0 => total
      case _ => filtered + "/" + total
    }) + ")"

  private def createView(source: TweetsProvider, title: String, include: Option[String]): View = {
    val fs = new FilterSet(session)
    include match {
      case Some(s) => fs.includeTextFilters.list ::= new TextFilter(s, false) 
      case None =>
    }
    val sto = new StatusTableOptions(true, true, true)
    val isMentions = source.isInstanceOf[MentionsProvider] // TODO do without this test
    val model = if (isMentions) {
      new StatusTableModel(sto, source, usersTableModel, fs, service, user.user, tagUsers) with Mentions
    } else {
      new StatusTableModel(sto, source, usersTableModel, fs, service, user.user, tagUsers)
    }
    val pane = new StatusPane(session, title, model, fs, this)
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    listenTo(model)
    val streamInfo = new View(title, model, pane)
    streamInfo.model.followerIds = followerIds
    views ::= streamInfo
    streamInfo
  }

  class TitleCreator(baseName: String) {
    var index = 0
    def create: String = {
      index += 1
      if (index == 1) baseName else baseName + index
    }
  }
  
  def createFollowingViewFor(include: String) = createView(tweetsProvider, folTitle.create, Some(include))

  def createFollowingView: View = createView(tweetsProvider, folTitle.create, None)
  
  def createRepliesViewFor(include: String) = createView(mentionsProvider, repTitle.create, Some(include))

  def createRepliesView: View = createView(mentionsProvider, repTitle.create, None)
  
  def componentTitle(comp: Component) = views.filter(s => s.pane == comp)(0).title
  
  def setFollowerIds(followerIds: List[String]) {
    this.followerIds = followerIds
    views.foreach(si => si.model.followerIds = followerIds)
  }
}

