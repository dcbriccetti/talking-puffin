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
object Main extends GUIApplication {
  BasicConfigurator.configure
  Logger.getRootLogger.setLevel(Level.INFO)
  private var username: String = ""
  private var password: String = ""
  
  /**
   * Creates the Swing frame.
   */
  def createTopFrame = {

    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)
    }
    Windows.tabbedPane = tabbedPane

    val streams = new Streams(username, password)
    Windows.streams = streams
    
    val frame = new Frame {
      title = "Simple Twitter Client"

      TagUsers.load

      contents = new BorderPanel {
        val toolBar = new MainToolBar(streams)
        peer.add(new FlowPanel(FlowPanel.Alignment.Left) {peer.add(toolBar)}.peer, BorderLayout.NORTH)
        add(tabbedPane, BorderPanel.Position.Center)
      }

      reactions += {
        case WindowClosing(_) => {
          StateRepository.set("highestId", streams.tweetsProvider.getHighestId)
          StateRepository.save
          TagUsers.save
          System.exit(1)
        }
      }

      peer.setLocationRelativeTo(null)
    }

    SwingInvoke.execSwingWorker({
      (new FriendsDataProvider(username, password).getUsers,
        new FollowersDataProvider(username, password).getUsers)
      }, 
      { (result: Tuple2[List[Node],List[Node]]) =>
      val (following, followers) = result 
              
      streams.usersModel.friends = following
      streams.usersModel.followers = followers
      streams.usersModel.usersChanged
 
      streams.setFollowerIds(getIds(followers))
              
      val paneTitle = "People (" + following.length + ", " + followers.length + ")"
      val pane = new FriendsFollowersPane(streams.apiHandlers, streams.usersModel, following, followers)
      tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
    })

    frame
  }
  
  private def getIds(users: List[Node]): List[String] = {
    users map (u => (u \ "id").text)
  }

  def main(args: Array[String]): Unit = {
    
    try {
      UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
      JFrame setDefaultLookAndFeelDecorated true
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
    val frame = createTopFrame
    frame.pack
    frame.visible = true
  }

}

class ApiHandlers(val sender: Sender, val follower: Follower)

