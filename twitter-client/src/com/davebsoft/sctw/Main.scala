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
 * Written to learn and teach Scala and the Twitter API, probably not
 * to create a real Twitter client.
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
        preferredSize = new Dimension(800, 600)
        
        val friendsTableModel = new StatusTableModel(tweetsProvider)
        pages.append(new TabbedPane.Page("Tweets", new StatusPane(friendsTableModel)))
        
        pages.append(new TabbedPane.Page("Following", new FriendsFollowersPane(
            new FriendsDataProvider(username, password))))
        
        pages.append(new TabbedPane.Page("Followers", new FriendsFollowersPane(
            new FollowersDataProvider(username, password))))
        
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
    
  override def main(args: Array[String]): Unit = {
    if (args.length >= 2) {
      username = args(0)
      password = args(1)
    } else if (LoginDialog.display) {
      username = LoginDialog.username
      password = LoginDialog.password
    }
    if (username.length > 0 && password.length > 0) {
      super.main(args)
    } else {
      println("Missing username and password")
      System.exit(1)
    }
  }
}