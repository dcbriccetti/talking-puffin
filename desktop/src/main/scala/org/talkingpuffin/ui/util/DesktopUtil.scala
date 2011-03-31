package org.talkingpuffin.ui.util

import java.awt.Desktop
import java.net.URI
import java.awt._
import javax.swing.ImageIcon
import org.talkingpuffin.Main
import actors.Actor._
import org.talkingpuffin.util.Loggable

object DesktopUtil extends Loggable {
  private case class Browse(uri: String)
  val browser = actor {
    while(true) {
      receive {
        case browse: Browse => {
          info("Before desktop.browse")
          Desktop.getDesktop.browse(new URI(browse.uri))
          info("After desktop.browse")
        }
      }
    }
  }

  val trayIcon: TrayIcon = SystemTray.isSupported match {
    case true =>
      val icon = new TrayIcon(new ImageIcon(getClass.getResource("/TalkingPuffin_16.png")).getImage,Main.title)
      SystemTray.getSystemTray.add(icon)
      icon
    case _ => null
  }

  def browse(uri: String) = if (Desktop.isDesktopSupported) browser ! Browse(uri)

  def notify(message: String, header: String) = if (SystemTray.isSupported) 
    trayIcon.displayMessage(header, message, TrayIcon.MessageType.INFO)
}
