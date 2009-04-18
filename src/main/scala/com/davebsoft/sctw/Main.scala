package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension}
import javax.swing.UIManager
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
  def top = {

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
    val statusPane  = new ToolbarStatusPane(tweetsTitle, tweetsModel,  apiHandlers, tweetsFilterSet)
    val repliesPane = new RepliesStatusPane(repliesTitle, repliesModel, apiHandlers, repliesFilterSet) {
      table.showColumn(3, false)
    }
    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)

      pages.append(new Page(tweetsTitle, statusPane))
      pages.append(new Page(repliesTitle, repliesPane))

    }

    val clearAction = statusPane.clearAction
    val frame = new Frame {
      title = "Simple Twitter Client"

      TagUsers.load

      listenTo(tweetsModel)
      listenTo(repliesModel)
      contents = tabbedPane

      reactions += {
        case WindowClosing(_) => {
          StateRepository.set("highestId", tweetsProvider.getHighestId)
          StateRepository.save
          TagUsers.save
          System.exit(1)
        }
        case TableContentsChanged(`tweetsModel`, filtered, total) => {
          tabbedPane.peer.setTitleAt(0, createTweetsTitle(tweetsTitle, filtered, total))
        }
        case TableContentsChanged(`repliesModel`, filtered, total) => {
          tabbedPane.peer.setTitleAt(1, createTweetsTitle(repliesTitle, filtered, total))
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
      tabbedPane.pages.append(new TabbedPane.Page(paneTitle, pane))
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
    val frame = top
    frame.pack
    frame.visible = true
  }

}

class ApiHandlers(val sender: Sender, val follower: Follower)
