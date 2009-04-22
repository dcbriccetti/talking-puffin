package com.davebsoft.sctw

import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension, BorderLayout}
import javax.swing.{JToolBar, UIManager, JFrame}
import org.apache.log4j.{Level, Logger, BasicConfigurator}
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
  BasicConfigurator.configure
  Logger.getRootLogger.setLevel(Level.INFO)
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

    contents = new BorderPanel {
      val toolBar = new MainToolBar(streams)
      peer.add(new FlowPanel(FlowPanel.Alignment.Left) {peer.add(toolBar)}.peer, BorderLayout.NORTH)
      add(tabbedPane, BorderPanel.Position.Center)
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

