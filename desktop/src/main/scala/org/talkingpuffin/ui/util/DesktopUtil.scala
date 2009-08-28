package org.talkingpuffin.ui.util

import java.awt.Desktop
import java.net.URI
import java.awt._
import javax.swing.ImageIcon
object DesktopUtil {
    val trayIcon = new TrayIcon(new ImageIcon(getClass.getResource("/TalkingPuffin_16.png")).getImage ,"Talking Puffin")
    if(SystemTray.isSupported){
        val tray = SystemTray.getSystemTray();
        tray.add(trayIcon)
    }
    def browse(uri: String) {
        if (Desktop.isDesktopSupported) {
            Desktop.getDesktop.browse(new URI(uri))
        }
    }
    def notify(message : String, header : String){
        println(message)
        if(SystemTray.isSupported){
            trayIcon.displayMessage(header, message, TrayIcon.MessageType.INFO)
        }
    }
  
}