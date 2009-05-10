package org.talkingpuffin

import _root_.scala.swing.event.{ButtonClicked, WindowClosing}
import filter.{FilterSet, TextFilter, TagUsers}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import java.awt.{Toolkit, Dimension, BorderLayout, Insets}
import java.util.prefs.Preferences
import javax.swing.border.{BevelBorder, EmptyBorder}
import javax.swing.{JToolBar, KeyStroke, ImageIcon, UIManager, JFrame}
import org.apache.log4j.Logger
import org.talkingpuffin.mac.QuitHandler
import scala.swing._
import scala.xml._

import TabbedPane._
import state.PreferencesFactory
import twitter._
import ui._
import ui.util.FetchRequest

/**
 * TalkingPuffin main object
 *
 * @author Dave Briccetti
 */
object Main {
  val log = Logger getLogger "Main"
  val title = "TalkingPuffin" 
  private var username: String = ""
  private var password: String = ""
  private var user: Node = _
  
  def main(args: Array[String]): Unit = {
    val props = System.getProperties
    props setProperty("apple.laf.useScreenMenuBar", "true")
    props setProperty("com.apple.mrj.application.apple.menu.about.name", Main.title)
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    JFrame setDefaultLookAndFeelDecorated true

    launchSession
  }
  
  def launchSession {
    def startUp(username: String, password: String, user: Node) {
      this.username = username
      this.password = password
      this.user = user

      new TopFrame(username, password, user) {
        pack
        visible = true
      }
    }

    new LoginDialog(new twitter.AuthenticationProvider, TopFrames.exitIfNoFrames, startUp).display
  }
}

class ApiHandlers(val sender: Sender, val follower: Follower)

class Session {
  val windows = new Windows
  val status = new Label(" ")
}

object Globals {
  var sessions: List[Session] = Nil
  class Options(var useAnimations: Boolean, var lookUpLocations: Boolean, var expandUrls: Boolean)
  val options = new Options(false, true, false)
}