package com.davebsoft.sctw.ui

import _root_.scala.actors.Actor
import scala.actors.Actor._
import java.net.URL
import javax.swing.ImageIcon

/**
 * Fetches pictures in the background, and calls a method in the event
 * dispatching thread when done.
 * @author Dave Briccetti
 */

case class FetchImage(val url: String)
class ImageReady(val url: String, val imageIcon: ImageIcon)
  
class PictureFetcher(processFinishedImage: (ImageReady) => Unit) extends Actor {

  def act = while(true) receive {
    case fi: FetchImage => 
      if (mailboxSize == 0) { 
        SwingInvoke.invokeLater({processFinishedImage(new ImageReady(fi.url, 
          new ImageIcon(new URL(fi.url))))})
      } // else ignore this request because of the newer one behind it
  }

  start
}