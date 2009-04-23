package com.davebsoft.sctw

import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension, BorderLayout, Insets}
import javax.swing.border.{BevelBorder, EmptyBorder}
import javax.swing.{JToolBar, UIManager, JFrame}
import org.apache.log4j.Logger
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
object Main {
  val log = Logger getLogger "Main"
  private var username: String = ""
  private var password: String = ""
  
  /**
   * The Swing frame.
   */
  class TopFrame extends Frame {

    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)
    }
    Windows.tabbedPane = tabbedPane

    val streams = new Streams(username, password)
    Windows.streams = streams
    
    title = "Simple Twitter Client"

    TagUsers.load

    contents = new GridBagPanel {
      val status = new Panel() {
        add(Status.message, new Constraints {anchor=Anchor.West; insets=new Insets(5,5,5,5)})
      }
      add(status, new Constraints {
        grid = (0,0); fill = GridBagPanel.Fill.Horizontal; weightx = 1;  
        })
      val toolBar = new MainToolBar(streams)
      peer.add(toolBar, new Constraints {
        grid = (0,1); fill = GridBagPanel.Fill.Horizontal; weightx = 1; }.peer)
      add(tabbedPane, new Constraints {
        grid = (0,2); fill = GridBagPanel.Fill.Both; weightx = 1; weighty = 1; })
    }

    reactions += {
      case WindowClosing(_) => {
        saveState
        System.exit(1)
      }
    }

    peer.setLocationRelativeTo(null)

    SwingInvoke.execSwingWorker({
      (new FriendsDataProvider(username, password).getUsers,
        new FollowersDataProvider(username, password).getUsers)
      }, 
      { (result: Tuple2[List[Node],List[Node]]) =>
      val (following, followers) = result 
              
      streams.usersModel.friends = following
      streams.usersModel.followers = followers
      streams.usersModel.usersChanged
 
      streams setFollowerIds (followers map (u => (u \ "id").text))
              
      val paneTitle = "People (" + following.length + ", " + followers.length + ")"
      val pane = new FriendsFollowersPane(streams.apiHandlers, streams.usersModel, following, followers)
      tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
    })
    
    private def saveState {
      val highFol = streams.tweetsProvider.getHighestId
      val highMen = streams.mentionsProvider.getHighestId
      log info("Saving last seen IDs. Following: " + highFol + ", mentions: " + highMen)
      StateRepository.set("highestId", highFol)
      StateRepository.set("highestMentionId", highMen)
      StateRepository.save
      TagUsers.save
    }
  }
  
  def main(args: Array[String]): Unit = {
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    JFrame setDefaultLookAndFeelDecorated true
    
    def startUp(userName: String, pwd: String) {
      username = userName
      password = pwd

      new TopFrame {
        pack
        visible = true
      }
    }

    new LoginDialog(new twitter.AuthenticationProvider, System.exit(1), startUp).display
  }
}

class ApiHandlers(val sender: Sender, val follower: Follower)

object Status {
  val message = new Label(" ")
}
