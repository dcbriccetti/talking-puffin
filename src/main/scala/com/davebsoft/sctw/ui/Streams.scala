package com.davebsoft.sctw.ui

import _root_.scala.swing.{TabbedPane, Component, Reactor}
import _root_.scala.xml.Node
import filter.{FilterSet, TextFilter}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import twitter.{Follower, RepliesProvider, TweetsProvider, Sender}
import state.StateRepository

case class StreamInfo(val title: String, val model: StatusTableModel, val pane: StatusPane)

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 * 
 * @author Dave Briccetti
 */

class Streams(username: String, password: String) extends Reactor {
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
      setTitleInParent(si.pane.peer, createTweetsTitle(si.title, filtered, total))
    }
  }

  private def setTitleInParent(pane: JComponent, title: String) {
    Windows.tabbedPane.peer.indexOfComponent(pane) match {
      case -1 => {
        SwingUtilities.getAncestorOfClass(classOf[JFrame], pane) match {
          case null =>
          case parent => parent.asInstanceOf[JFrame].setTitle(title)
        }
      }
      case tabbedPaneIndex => Windows.tabbedPane.peer.setTitleAt(tabbedPaneIndex, title)
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
    val pane = new TweetsStatusPane(title, model, apiHandlers, fs, this)
    Windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    listenTo(model)
    val streamInfo = new StreamInfo(title, model, pane)
    streamInfoList ::= streamInfo
    streamInfo
  }

  def createStreamFor(include: String) = createStream(Some(include))

  def createStream: StreamInfo = createStream(None)
  
  def componentTitle(comp: Component) = streamInfoList.filter(s => s.pane == comp)(0).title
}

