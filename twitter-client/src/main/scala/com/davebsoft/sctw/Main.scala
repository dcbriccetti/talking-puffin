package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}
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
object Main extends SimpleGUIApplication {
  
  private var username: String = ""
  private var password: String = ""
  
  /**
   * Creates the Swing frame.
   */
  def top = {
  
    new Frame {
      title = "Simple Twitter Client"

      TagUsers.load
      val tweetsProvider = new TweetsProvider(username, password, StateRepository.get("highestId", null))

      contents = new TabbedPane() {
        preferredSize = new Dimension(900, 600)
        
        val friendsTableModel = new StatusTableModel(tweetsProvider, username)
        pages.append(new TabbedPane.Page("Tweets", new StatusPane(friendsTableModel)))
        
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
    
  override def main(args: Array[String]): Unit = {
    
    def startup(userName: String, pwd: String) {
        username = userName
        password = pwd
      if (username.length > 0 && password.length > 0) {
        setupUI
      } else {
        println("Missing username and password")
        System.exit(1)
      }	
    }
    
    val login = new LoginDialog(new twitter.AuthenticationProvider, startup)
    login.display
  }
  
  def setupUI {
    init(); 
    top.pack(); 
    top.visible = true 
  }
  
}