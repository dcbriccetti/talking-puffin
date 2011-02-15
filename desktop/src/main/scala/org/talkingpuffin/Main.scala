package org.talkingpuffin

import javax.swing.{UIManager, JFrame}
import mac.MacInit
import twitter.{CredentialsRepository, AuthenticatedSession}
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
    launchAllSessions
  }

  def launchAllSessions {
      CredentialsRepository.getAll.foreach(cred => new TopFrame(AuthenticatedSession.logIn(Some(cred))))
  }

  def launchSession {
      new TopFrame(AuthenticatedSession.logIn(None))
  }
}
