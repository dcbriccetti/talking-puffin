package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension, BorderLayout}
import javax.swing.{JToolBar, UIManager, JFrame}
import scala.swing._
import scala.xml._
import TabbedPane._
import state.StateRepository
import twitter._
import ui._

/**
 * “Simple Twitter Client”
 *
 * Your feedback is welcome!
 *
 * @Author Dave Briccetti, daveb@davebsoft.com, @dcbriccetti
 */
object Main extends GUIApplication {
  private var username: String = ""
  private var password: String = ""
  private val tweetsTitle = "Tweets"
  private val repliesTitle = "Replies"
  
  /**
   * Creates the Swing frame.
   */
  def createTopFrame = {

    val streams = new Streams(username, password)
    Windows.streams = streams
    val statusPane  = new TweetsStatusPane(tweetsTitle,  streams.tweetsModel,  
      streams.apiHandlers, streams.tweetsFilterSet, streams)
    val repliesPane = new RepliesStatusPane(repliesTitle, streams.repliesModel, 
      streams.apiHandlers, streams.repliesFilterSet, streams) {
      table.showColumn(3, false)
    }
    
    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)
      pages += new Page(tweetsTitle, statusPane)
      streams.streamInfoList ::= new StreamInfo(tweetsTitle, streams.tweetsModel, statusPane)
      pages += new Page(repliesTitle, repliesPane)
      streams.streamInfoList ::= new StreamInfo(repliesTitle, streams.repliesModel, repliesPane)
    }
    Windows.tabbedPane = tabbedPane

    val clearAction = statusPane.clearAction
    val frame = new Frame {
      title = "Simple Twitter Client"

      TagUsers.load

      contents = new BorderPanel {
        val toolBar = new JToolBar
        toolBar.setFloatable(false)
        val newStreamAction = new Action("New Stream") {
          toolTip = "Creates a new stream of tweets"
          def apply = {
            streams.createStream
          }
        }
        toolBar.add(newStreamAction.peer)
        peer.add(toolBar, BorderLayout.NORTH)
        add(tabbedPane, BorderPanel.Position.Center)
      }

      reactions += {
        case WindowClosing(_) => {
          StateRepository.set("highestId", streams.tweetsProvider.getHighestId)
          StateRepository.save
          TagUsers.save
          System.exit(1)
        }
      }

      peer.setLocationRelativeTo(null)
      statusPane.requestFocusForTable
    }

    SwingInvoke.execSwingWorker({
      (new FriendsDataProvider(username, password).getUsers,
        new FollowersDataProvider(username, password).getUsers)
    }, 
      { (result: Tuple2[List[Node],List[Node]]) =>
      val following = result._1 
      val followers = result._2 
              
      streams.usersModel.friends = following
      streams.usersModel.followers = followers
      streams.usersModel.usersChanged
              
      streams.tweetsModel.followerIds = getIds(followers)
      streams.repliesModel.followerIds = getIds(followers)
              
      val paneTitle = "People (" + following.length + ", " + followers.length + ")"
      val pane = new FriendsFollowersPane(streams.apiHandlers, streams.usersModel, following, followers)
      tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
    })

    frame
  }

  private def getIds(users: List[Node]): List[String] = {
    users map (u => (u \ "id").text)
  }

  def main(args: Array[String]): Unit = {
    
    try {
      UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
      JFrame setDefaultLookAndFeelDecorated true
    } catch {
      case e: Exception => // Ignore
    }
    
    def startUp(userName: String, pwd: String) {
      username = userName
      password = pwd
      setUpUi
    }

    def shutDown = System.exit(1)

    new LoginDialog(new twitter.AuthenticationProvider, shutDown, startUp).display
  }
  
  def setUpUi {
    init
    val frame = createTopFrame
    frame.pack
    frame.visible = true
  }

}

class ApiHandlers(val sender: Sender, val follower: Follower)

