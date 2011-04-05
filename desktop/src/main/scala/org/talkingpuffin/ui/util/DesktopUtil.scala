package org.talkingpuffin.ui.util

import java.awt.Desktop
import java.net.URI
import java.awt._
import javax.swing.ImageIcon
import akka.actor._
import akka.actor.Actor._
import org.talkingpuffin.Main
import org.talkingpuffin.util.Loggable

object DesktopUtil extends Loggable {
  private case class Browse(uri: String)

  private lazy val browser = actorOf(new Actor() {
    def receive = {
      case browse: Browse => {
        info("Before desktop.browse")
        Desktop.getDesktop.browse(new URI(browse.uri))
        info("After desktop.browse")
      }
    }
  }).start()

  private lazy val trayIcon: Option[TrayIcon] =
    if (SystemTray.isSupported) {
      val icon = new TrayIcon(new ImageIcon(getClass.getResource("/TalkingPuffin_16.png")).getImage,Main.title)
      SystemTray.getSystemTray.add(icon)
      Some(icon)
    } else None

  def browse(uri: String) = if (Desktop.isDesktopSupported) browser ! Browse(uri)

  def notify(message: String, header: String) =
    trayIcon.foreach(_.displayMessage(header, message, TrayIcon.MessageType.INFO))
}
