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
    val following = new FriendsDataProvider(username, password).getUsers
    val followers = new FollowersDataProvider(username, password).getUsers
    val usersModel = new UsersTableModel(following, followers)
    val tweetsFilterSet = new FilterSet
    val tweetsModel  = new StatusTableModel(new StatusTableOptions(true), tweetsProvider,
      usersModel, getIds(followers), tweetsFilterSet, username)
    val repliesFilterSet = new FilterSet
    val repliesModel = new StatusTableModel(new StatusTableOptions(false), repliesProvider, 
      usersModel, getIds(followers), repliesFilterSet, username) with Replies
    val statusPane  = new ToolbarStatusPane(tweetsTitle, tweetsModel,  apiHandlers, tweetsFilterSet)
    val repliesPane = new RepliesStatusPane(repliesTitle, repliesModel, apiHandlers, repliesFilterSet) {
      table.showColumn(3, false)
    }

    val clearAction = statusPane.clearAction
    new Frame {
      title = "Simple Twitter Client"

      TagUsers.load

      val tabbedPane = new TabbedPane() {
        preferredSize = new Dimension(900, 600)

        pages.append(new Page(tweetsTitle, statusPane))
        pages.append(new Page(repliesTitle, repliesPane))

        pages.append(new Page("People (" + following.length + ", " + followers.length + ")", 
          new FriendsFollowersPane(apiHandlers, usersModel, following, followers)))
      }
      listenTo(tabbedPane.selection)
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
