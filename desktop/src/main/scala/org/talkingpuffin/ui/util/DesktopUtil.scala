package org.talkingpuffin.ui.util

import java.awt.Desktop
import java.net.URI
import java.awt._
import javax.swing.ImageIcon

object DesktopUtil {
  val trayIcon: TrayIcon = SystemTray.isSupported match {
    case true =>
      val icon = new TrayIcon(new ImageIcon(getClass.getResource("/TalkingPuffin_16.png")).getImage,Main.title)
      SystemTray.getSystemTray.add(icon)
      icon
    case _ => null
  }

  def browse(uri: String) = if (Desktop.isDesktopSupported) Desktop.getDesktop.browse(new URI(uri))

  def notify(message: String, header: String) = if (SystemTray.isSupported) 
    trayIcon.displayMessage(header, message, TrayIcon.MessageType.INFO)
}