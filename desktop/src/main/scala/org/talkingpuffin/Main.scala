package org.talkingpuffin

import javax.swing.{UIManager, JFrame}
import mac.{MacInit}
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
