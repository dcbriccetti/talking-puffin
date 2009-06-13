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
 */
object Main {
  val log = Logger getLogger "Main"
  val title = "TalkingPuffin" 
  private var username: String = ""
  private var password: String = ""
  private var user: TwitterSession = _
  
  def main(args: Array[String]): Unit = {
    val props = System.getProperties
    props setProperty("apple.laf.useScreenMenuBar", "true")
    props setProperty("com.apple.mrj.application.apple.menu.about.name", Main.title)
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    JFrame setDefaultLookAndFeelDecorated true

    launchSession
  }
  
  def launchSession {
    def startUp(username: String, password: String, user: AuthenticatedSession) {
      this.username = username
      this.password = password
      this.user = user

      new TopFrame(username, password, user) {
        pack
        visible = true
      }
    }

    new LoginDialog(TopFrames.exitIfNoFrames, startUp).display
  }
}

class Session(val twitterSession:AuthenticatedSession) {
  val windows = new Windows
  val status = new Label(" ")
  var progress = new LongOpListener {  // Will be rebound to real listener
    def stopOperation = null
    def startOperation = null
  }
}

object Globals {
  var sessions: List[Session] = Nil
}

