package com.davebsoft.sctw.ui

import _root_.scala.swing.{TabbedPane, Reactor}
import _root_.scala.xml.Node
import filter.{FilterSet, TextFilter}
import twitter.{Follower, RepliesProvider, TweetsProvider, Sender}
import state.StateRepository

case class StreamInfo(val title: String, val model: StatusTableModel, val pane: StatusPane)

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 * 
 * @author Dave Briccetti
 */

class Streams(username: String, password: String) extends Reactor {
  var tabbedPane: TabbedPane = _
  val tweetsProvider = new TweetsProvider(username, password, StateRepository.get("highestId", null))
  val repliesProvider = new RepliesProvider(username, password)
  val apiHandlers = new ApiHandlers(new Sender(username, password), new Follower(username, password))
  val usersModel = new UsersTableModel(List[Node](), List[Node]())
  
  val tweetsFilterSet = new FilterSet
  val tweetsModel = new StatusTableModel(new StatusTableOptions(true), tweetsProvider,
    usersModel, tweetsFilterSet, username)
  
  val repliesFilterSet = new FilterSet
  val repliesModel = new StatusTableModel(new StatusTableOptions(false), repliesProvider, 
    usersModel, repliesFilterSet, username) with Replies
  
  var streamInfoList = List[StreamInfo]()
  
  listenTo(tweetsModel)
  listenTo(repliesModel)
  reactions += {
    case TableContentsChanged(model, filtered, total) => {
      val si = streamInfoList.filter(s => s.model == model)(0)
      tabbedPane.peer.setTitleAt(tabbedPane.peer.indexOfComponent(si.pane.peer), 
        createTweetsTitle(si.title, filtered, total))
    }
  }

  private def createTweetsTitle(paneTitle: String, filtered: Int, total: Int): String = {
    paneTitle + " (" + filtered + "/" + total + ")"
  }

  var newStreamIndex = 1
  def createStream(include: Option[String]): StreamInfo = {
    val fs = new FilterSet
    include match {
      case Some(s) => fs.includeTextFilters.add(new TextFilter(s, false)) 
      case None =>
    }
    newStreamIndex += 1
    val title = "Tweets" + newStreamIndex
    val provider = new TweetsProvider(username, password, StateRepository.get("highestId", null))
    val model  = new StatusTableModel(new StatusTableOptions(true), 
      provider, usersModel, fs, username)
    val pane = new ToolbarStatusPane(title, model, apiHandlers, fs, this)
    tabbedPane.pages += new TabbedPane.Page(title, pane)
    listenTo(model)
    val streamInfo = new StreamInfo(title, model, pane)
    streamInfoList ::= streamInfo
    streamInfo
  }

  def createStreamFor(include: String) = createStream(Some(include))

  def createStream: StreamInfo = createStream(None)
  
}

