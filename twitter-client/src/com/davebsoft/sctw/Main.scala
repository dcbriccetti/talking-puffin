package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged}
import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.table.{DefaultTableModel, AbstractTableModel}
import javax.swing.{SwingUtilities, Timer}
import java.util.{ArrayList,Collections}
import scala.swing._
import scala.xml._
import ui.{StatusTableModel, StatusPane}

/**
 * Continually displays Twitter statuses in a Swing JTable.
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
   * Creates the Swing frame, which consists of a TabbedPane with two panes, 
   * with each pane containing a JTable inside a JScrollPane.
   */
  def top = {
    val friendsStatusDataProvider = new FriendsStatusDataProvider(username, password)
    val publicStatusDataProvider = new PublicStatusDataProvider()
    
    val friendsModel = new StatusTableModel(friendsStatusDataProvider)
    val publicModel = new StatusTableModel(publicStatusDataProvider)

  
    new MainFrame {
      title = "Too-Simple Twitter Client"
      
      contents = new TabbedPane() {
        preferredSize = new Dimension(750, 600)
        pages.append(new TabbedPane.Page("Friends", new StatusPane(friendsModel)))
        pages.append(new TabbedPane.Page("Public", new StatusPane(publicModel)))
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