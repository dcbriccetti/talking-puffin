package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension}
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
  
  /**
   * Creates the Swing frame.
   */
  def top = {

    val filterSet = new FilterSet
    val tweetsProvider = new TweetsProvider(username, password, StateRepository.get("highestId", null))
    val repliesProvider = new RepliesProvider(username, password)
    val tweetSender = new Sender(username, password)
    val followers = new FollowersDataProvider(username, password).getUsers
    val tweetsModel  = new StatusTableModel(tweetsProvider,  getIds(followers), filterSet, username)
    val repliesModel = new StatusTableModel(repliesProvider, getIds(followers), filterSet, username) with Replies
    val filtersPane = new FiltersPane(tweetsModel, filterSet)
    val statusPane  = new ToolbarStatusPane(tweetsModel,  tweetSender, filtersPane)
    val repliesPane = new StatusPane(repliesModel, tweetSender, filtersPane)

    val clearAction = statusPane.clearAction
    new Frame {
      title = "Simple Twitter Client"

      TagUsers.load

      val filtersPage = new Page("Filters", filtersPane)

      val tabbedPane = new TabbedPane() {
        preferredSize = new Dimension(900, 600)

        pages.append(new Page("Tweets", statusPane))
        pages.append(new Page("Replies", repliesPane))

        val following = new FriendsDataProvider(username, password).getUsers
        pages.append(new Page("People (" + following.length + "/" + followers.length + ")", 
          new FriendsFollowersPane(following, followers)))
        pages.append(filtersPage)
      }
      listenTo(tabbedPane.selection)
      listenTo(tweetsModel)
      contents = tabbedPane
      var lastSelectedPane = tabbedPane.selection.page

      reactions += {
        case WindowClosing(_) => {
          StateRepository.set("highestId", tweetsProvider.getHighestId)
          StateRepository.save
          TagUsers.save
          System.exit(1)
        }
        case SelectionChanged(`tabbedPane`) => {
          val selectedPage = tabbedPane.selection.page
          if (lastSelectedPane == filtersPage && selectedPage != filtersPage) {
            filtersPane.applyChanges
          }
          lastSelectedPane = selectedPage
        }
        case TableContentsChanged(filtered, total) => {
          tabbedPane.peer.setTitleAt(0, createTweetsTitle(filtered, total))
        }
      }

      peer.setLocationRelativeTo(null)
    }
  }

  private def createTweetsTitle(filtered: Int, total: Int): String = {
    "Tweets (" + filtered + "/" + total + ")"
  }
  
  private def getIds(users: List[Node]): List[String] = {
    users map (u => (u \ "id").text)
  }

  def main(args: Array[String]): Unit = {
    
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