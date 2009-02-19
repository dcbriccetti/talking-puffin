package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged}
import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.table.{DefaultTableModel, AbstractTableModel}
import javax.swing.{SwingUtilities, Timer}
import java.util.{ArrayList,Collections}
import scala.swing._
import scala.xml._
import twitter.{FriendsDataProvider, PublicStatusDataProvider, FollowersDataProvider, FriendsStatusDataProvider}
import ui.{StatusTableModel, FiltersPane, StatusPane, FriendsFollowersPane}
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
  
  private var username: String = null
  private var password: String = null
  
  /**
   * Creates the Swing frame.
   */
  def top = {
  
    new MainFrame {
      title = "Simple Twitter Client"
      
      contents = new TabbedPane() {
        preferredSize = new Dimension(800, 600)
        
        val friendsTableModel = new StatusTableModel(new FriendsStatusDataProvider(username, password))
        pages.append(new TabbedPane.Page("Friends’ Tweets", new StatusPane(friendsTableModel)))
        
        pages.append(new TabbedPane.Page("Public Tweets", new StatusPane(
            new StatusTableModel(new PublicStatusDataProvider))))
        
        pages.append(new TabbedPane.Page("Following", new FriendsFollowersPane(
            new FriendsDataProvider(username, password))))
        
        pages.append(new TabbedPane.Page("Followers", new FriendsFollowersPane(
            new FollowersDataProvider(username, password))))
        
        pages.append(new TabbedPane.Page("Filters", new FiltersPane(friendsTableModel)))
      }
      
      peer.setLocationRelativeTo(null)
    }
  }
    
  override def main(args: Array[String]): Unit = {
    if (args.length >= 2) {
      username = args(0)
      password = args(1)
      super.main(args)
    } else {
      println("Missing username and password")
    }
  }
}