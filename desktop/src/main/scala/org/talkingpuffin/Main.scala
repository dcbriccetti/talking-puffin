package org.talkingpuffin

import javax.swing.{UIManager, JFrame}
import mac.{MacInit}
import ui.{TopFrames, LoginDialog, TopFrame}
import twitter.AuthenticatedSession

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

    /**
     * Called by LoginDialog if login is successful, to create and show a TopFrame.
     */
    def startUp(service: String, twitterSession: AuthenticatedSession) {
      new TopFrame(service, twitterSession) {
        pack
        visible = true
        setFocus
      }
    }

    new LoginDialog(TopFrames.exitIfNoFrames, startUp).display
  }
}
