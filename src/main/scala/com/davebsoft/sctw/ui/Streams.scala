package com.davebsoft.sctw.ui

import _root_.scala.swing.{TabbedPane, Component, Reactor}
import _root_.scala.xml.Node
import filter.{FilterSet, TextFilter}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import twitter.{Follower, MentionsProvider, TweetsProvider, Sender}
import state.StateRepository

case class StreamInfo(val title: String, val model: StatusTableModel, val pane: StatusPane)

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 * 
 * @author Dave Briccetti
 */

class Streams(session: Session, username: String, password: String) extends Reactor {
  val tweetsProvider = new TweetsProvider(username, password, 
    Some(StateRepository.get(username + "-highestId", null)), "Following")
  val mentionsProvider = new MentionsProvider(username, password, 
    Some(StateRepository.get(username + "-highestMentionId", null)))
  val apiHandlers = new ApiHandlers(new Sender(username, password), new Follower(username, password))
  val usersModel = new UsersTableModel(List[Node](), List[Node]())
  
  var streamInfoList = List[StreamInfo]()
  
  val folTitle = new TitleCreator("Following")
  val repTitle = new TitleCreator("Mentions")

  var followerIds = List[String]()
  
  reactions += {
    case TableContentsChanged(model, filtered, total) => {
      if (streamInfoList.length == 0) println("streamInfoList is empty. Ignoring table contents changed.")
      else {
        val filteredList = streamInfoList.filter(_.model == model)
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

  private def createTweetsTitle(paneTitle: String, filtered: Int, total: Int): String = {
    paneTitle + " (" + filtered + "/" + total + ")"
  }

  private def createStream(source: TweetsProvider, title: String, include: Option[String]): StreamInfo = {
    val fs = new FilterSet
    include match {
      case Some(s) => fs.includeTextFilters.add(new TextFilter(s, false)) 
      case None =>
    }
    val sto = new StatusTableOptions(true)
    val isMentions = source.isInstanceOf[MentionsProvider] // TODO do without this test
    val model = if (isMentions) {
      new StatusTableModel(sto, source, usersModel, fs, username) with Mentions
    } else {
      new StatusTableModel(sto, source, usersModel, fs, username)
    }
    val pane = new StatusPane(session, title, model, apiHandlers, fs, this)
    if (isMentions) {
      pane.table.showColumn(3, false)
    }
    pane.requestFocusForTable
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    listenTo(model)
    val streamInfo = new StreamInfo(title, model, pane)
    streamInfo.model.followerIds = followerIds
    streamInfoList ::= streamInfo
    streamInfo
  }

  class TitleCreator(baseName: String) {
    var index = 0
    def create: String = {
      index += 1
      if (index == 1) baseName else baseName + index
    }
  }
  
  def createFollowingViewFor(include: String) = createStream(tweetsProvider, folTitle.create, Some(include))

  def createFollowingView: StreamInfo = createStream(tweetsProvider, folTitle.create, None)
  
  def createRepliesViewFor(include: String) = createStream(mentionsProvider, repTitle.create, Some(include))

  def createRepliesView: StreamInfo = createStream(mentionsProvider, repTitle.create, None)
  
  def componentTitle(comp: Component) = streamInfoList.filter(s => s.pane == comp)(0).title
  
  def setFollowerIds(followerIds: List[String]) {
    this.followerIds = followerIds
    streamInfoList.foreach(si => si.model.followerIds = followerIds)
  }
}

