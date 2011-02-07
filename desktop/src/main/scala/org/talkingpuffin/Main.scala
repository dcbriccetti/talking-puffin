package org.talkingpuffin

import javax.swing.{UIManager, JFrame}
import twitter.AuthenticatedSession
import mac.MacInit
import ui.{TopFrame}

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

  /**
   * Presents the login dialog, and if login is successful, creates a TopFrame.
   */
  def launchSession {
      new TopFrame("Twitter", new AuthenticatedSession())
  }
}
