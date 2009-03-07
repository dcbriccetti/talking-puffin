package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Dimension}
import javax.swing.table.{DefaultTableModel, AbstractTableModel}
import javax.swing.{SwingUtilities, Timer}
import java.util.{ArrayList,Collections}
import scala.swing._
import scala.xml._
import twitter.{FriendsDataProvider, FollowersDataProvider, TweetsProvider}
import ui.{StatusTableModel, FiltersPane, StatusPane, FriendsFollowersPane, LoginDialog}
import filter.TagUsers
import state.StateRepository

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
  
     val tweetsProvider = new TweetsProvider(username, password, StateRepository.get("highestId", null))
     val friendsTableModel = new StatusTableModel(tweetsProvider, username)
     val statusPane = new StatusPane(friendsTableModel)
 
     new Frame {
      title = "Simple Twitter Client"
      menuBar = new MenuBar {
        val tweetMenu = new Menu("Tweets")
        tweetMenu.contents += new MenuItem(new Action("Clear") {def apply=statusPane.clearTweets})
        tweetMenu.contents += new MenuItem(new Action("Last 200") {
          toolTip = "Loads the last 200 of your “following” tweets"
          def apply={
            statusPane.clearSelection
            friendsTableModel.loadLastSet
          }
        })
        contents += tweetMenu
      }

      TagUsers.load

      contents = new TabbedPane() {
        preferredSize = new Dimension(900, 600)
        
        pages.append(new TabbedPane.Page("Tweets", statusPane))
        
        val following = new FriendsDataProvider(username, password).getUsers
        val followers = new FollowersDataProvider(username, password).getUsers
        pages.append(new TabbedPane.Page("Following", new FriendsFollowersPane(following, getIds(followers))))
        pages.append(new TabbedPane.Page("Followers", new FriendsFollowersPane(followers, getIds(following))))
        
        pages.append(new TabbedPane.Page("Filters", new FiltersPane(friendsTableModel)))
      }

      reactions += {
        case WindowClosing(_) => { 
          StateRepository.set("highestId", tweetsProvider.getHighestId) 
          StateRepository.save
          TagUsers.save
          System.exit(1) 
        }
      }
      
      peer.setLocationRelativeTo(null)
    }
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
    
    val login = new LoginDialog(new twitter.AuthenticationProvider, shutDown, startUp)
    login.display
  }
  
  def shutDown {
    System.exit(1)
  }
  
  def setUpUi {
    init(); 
    top.pack(); 
    top.visible = true 
  }
  
}