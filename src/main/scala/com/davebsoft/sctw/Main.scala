package com.davebsoft.sctw

import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.{ButtonClicked, SelectionChanged, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Dimension, BorderLayout, Insets}
import javax.swing.border.{BevelBorder, EmptyBorder}
import javax.swing.{JToolBar, ImageIcon, UIManager, JFrame}
import org.apache.log4j.Logger
import scala.swing._
import scala.xml._

import TabbedPane._
import state.StateRepository
import twitter._
import ui._

/**
 * “TalkingPuffin”
 *
 * Your feedback is welcome!
 *
 * @Author Dave Briccetti, daveb@davebsoft.com, @dcbriccetti
 */
object Main {
  val log = Logger getLogger "Main"
  val title = "TalkingPuffin" 
  private var username: String = ""
  private var password: String = ""
  
  object TopFrame {
    var numFrames = 0
  }
  
  /**
   * The Swing frame.
   */
  class TopFrame(username: String) extends Frame {

    TopFrame.numFrames += 1
    val session = new Session()
    Globals.sessions ::= session
    iconImage = new ImageIcon(getClass.getResource("/TalkingPuffin.png")).getImage
    
    val tabbedPane = new TabbedPane() {
      preferredSize = new Dimension(900, 600)
    }
    session.windows.tabbedPane = tabbedPane

    val streams = new Streams(session, username, password)
    session.windows.streams = streams
    
    title = Main.title + " - " + username
    
    menuBar = new MenuBar {
      contents += new Menu("Session") {
        contents += new MenuItem(Action("New...") { launchSession })
      }
    }

    TagUsers.load

    contents = new GridBagPanel {
      val status = new Panel() {
        add(session.status, new Constraints {anchor=Anchor.West; insets=new Insets(5,5,5,5)})
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
        Globals.sessions = Globals.sessions remove(s => s == session) // TODO is this best way?
        saveState
        TopFrame.numFrames -= 1
        if (TopFrame.numFrames == 0) System.exit(1)
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
      val pane = new FriendsFollowersPane(session, streams.apiHandlers, streams.usersModel, following, followers)
      tabbedPane.pages += new TabbedPane.Page(paneTitle, pane)
    })
    
    private def saveState {
      val highFol = streams.tweetsProvider.getHighestId
      val highMen = streams.mentionsProvider.getHighestId
      log info("Saving last seen IDs. Following: " + highFol + ", mentions: " + highMen)
      StateRepository.set(username + "-highestId", highFol)
      StateRepository.set(username + "-highestMentionId", highMen)
      StateRepository.save
      TagUsers.save
    }
  }
  
  def main(args: Array[String]): Unit = {
    val props = System.getProperties
    props setProperty("apple.laf.useScreenMenuBar", "true")
    props setProperty("com.apple.mrj.application.apple.menu.about.name", Main.title)
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    JFrame setDefaultLookAndFeelDecorated true

    launchSession
  }
  
  def launchSession {
    def startUp(userName: String, pwd: String) {
      username = userName
      password = pwd

      new TopFrame(username) {
        pack
        visible = true
      }
    }

    new LoginDialog(new twitter.AuthenticationProvider, System.exit(1), startUp).display
  }
}

class ApiHandlers(val sender: Sender, val follower: Follower)

class Session {
  val windows = new Windows
  val status = new Label(" ")
}

object Globals {
  var sessions: List[Session] = Nil
}