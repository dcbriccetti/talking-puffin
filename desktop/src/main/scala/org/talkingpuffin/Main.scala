package org.talkingpuffin

import java.util.prefs.Preferences
import javax.swing.{UIManager, JFrame}
import mac.{MacInit, QuitHandler}
import scala.swing.{Label}

import state.PreferencesFactory
import twitter._
import ui._

/**
 * TalkingPuffin main object
 */
object Main {
  val title = "TalkingPuffin" 
  
  def main(args: Array[String]): Unit = {
    MacInit init Main.title
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    JFrame setDefaultLookAndFeelDecorated true

    launchSession
  }
  
  def launchSession {
    def startUp(service: String, twitterSession: AuthenticatedSession) =
      new TopFrame(service, twitterSession) {
        pack
        visible = true
        setFocus
      }

    new LoginDialog(TopFrames.exitIfNoFrames, startUp).display
  }
}

class Session(val twitterSession: AuthenticatedSession) {
  val windows = new Windows
  val status = new Label(" ")
  var progress: LongOpListener = null
  def userPrefs: Preferences = windows.streams.prefs
}

object Globals {
  var sessions: List[Session] = Nil
}
