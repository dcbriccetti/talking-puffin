package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TagUsers}
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

    val tweetsProvider = new TweetsProvider(username, password, StateRepository.get("highestId", null))
    val repliesProvider = new RepliesProvider(username, password)
    val apiHandlers = new ApiHandlers(new Sender(username, password), new Follower(username, password))
    val usersModel = new UsersTableModel(List[Node](), List[Node]())
    val tweetsFilterSet = new FilterSet
    val tweetsModel  = new StatusTableModel(new StatusTableOptions(true), tweetsProvider,
      usersModel, tweetsFilterSet, username)
    val repliesFilterSet = new FilterSet
    val repliesModel = new StatusTableModel(new StatusTableOptions(false), repliesProvider, 
      usersModel, repliesFilterSet, username) with Replies
    val statusPane  = new ToolbarStatusPane(tweetsTitle,  tweetsModel,  apiHandlers, tweetsFilterSet)
    val repliesPane = new RepliesStatusPane(repliesTitle, repliesModel, apiHandlers, repliesFilterSet) {
      table.showColumn(3, false)
    }
    var streams = List[StreamInfo]()
    
    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)
      pages += new Page(tweetsTitle, statusPane)
      streams ::= new StreamInfo(tweetsTitle, tweetsModel, statusPane)
      pages += new Page(repliesTitle, repliesPane)
      streams ::= new StreamInfo(repliesTitle, repliesModel, repliesPane)
    }

    val clearAction = statusPane.clearAction
    val frame = new Frame {
      title = "Simple Twitter Client"
      val newFrame = this

      TagUsers.load

      listenTo(tweetsModel)
      listenTo(repliesModel)
      contents = new BorderPanel {
        val toolBar = new JToolBar
        toolBar.setFloatable(false)
        val newStreamAction = new Action("New Stream") {
          toolTip = "Creates a new stream of tweets"
          var index = 1
          def apply = {
            val fs = new FilterSet
            index += 1
            val title = "Tweets" + index
            val provider = new TweetsProvider(username, password, StateRepository.get("highestId", null))
            val model  = new StatusTableModel(new StatusTableOptions(true), 
              provider, usersModel, fs, username)
            val pane = new ToolbarStatusPane(title, model, apiHandlers, fs)
            tabbedPane.pages += new TabbedPane.Page(title, pane)
            newFrame.listenTo(model)
            streams ::= new StreamInfo(title, model, pane)
          }
        }
        toolBar.add(newStreamAction.peer)
        peer.add(toolBar, BorderLayout.NORTH)
        add(tabbedPane, BorderPanel.Position.Center)
      }

      reactions += {
        case WindowClosing(_) => {
          StateRepository.set("highestId", tweetsProvider.getHighestId)
          StateRepository.save
          TagUsers.save
          System.exit(1)
        }
        case TableContentsChanged(model, filtered, total) => {
          val si = streams.filter(s => s.model == model)(0)
          tabbedPane.peer.setTitleAt(tabbedPane.peer.indexOfComponent(si.pane.peer), 
            createTweetsTitle(si.title, filtered, total))
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
              
      usersModel.friends = following
      usersModel.followers = followers
      usersModel.usersChanged
              
      tweetsModel.followerIds = getIds(followers)
      repliesModel.followerIds = getIds(followers)
              
      val paneTitle = "People (" + following.length + ", " + followers.length + ")"
      val pane = new FriendsFollowersPane(apiHandlers, usersModel, following, followers)
      tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
    })

    frame
  }

  private def createTweetsTitle(paneTitle: String, filtered: Int, total: Int): String = {
    paneTitle + " (" + filtered + "/" + total + ")"
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

case class StreamInfo(val title: String, val model: StatusTableModel, val pane: StatusPane)