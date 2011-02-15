package org.talkingpuffin

import javax.swing.{UIManager, JFrame}
import mac.MacInit
import twitter.{Credentials, CredentialsRepository, AuthenticatedSession}
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

  def launchNewSession(credentials: Option[Credentials] = None) = new TopFrame(AuthenticatedSession.logIn(credentials))

  private def launchAllSessions = CredentialsRepository.getAll.foreach(cred => launchNewSession(Some(cred)))
}
