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
   * Creates a TopFrame.
   */
  def launchSession {
      new TopFrame(new AuthenticatedSession())
  }
}
