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
  val imageCache = scala.collection.mutable.Map.empty[String, ImageIcon]
  
  def act = while(true) receive {
    case fi: FetchImage => 
      if (mailboxSize == 0) {
        val icon = imageCache.get(fi.url) match { 
          case Some(imageIcon) => imageIcon
          case None => {
            val newIcon = new ImageIcon(new URL(fi.url))
            if (imageCache.size > 1000) imageCache.clear // TODO clear LRU instead?
            imageCache(fi.url) = newIcon
            newIcon
          }
        }
        SwingInvoke.invokeLater({processFinishedImage(new ImageReady(fi.url, icon))})
      } // else ignore this request because of the newer one behind it
  }

  start
}