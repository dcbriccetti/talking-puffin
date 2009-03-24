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

object PictureFetcher {
  val imageCache = scala.collection.mutable.Map.empty[String, ImageIcon]
}

case class FetchImage(val url: String, val id: Object)
class ImageReady(val url: String, val id: Object, val imageIcon: ImageIcon)
  
class PictureFetcher(processFinishedImage: (ImageReady) => Unit, processAll: Boolean) extends Actor {
  
  def act = while(true) receive {
    case fi: FetchImage =>
      if (mailboxSize == 0 || processAll) {
        val icon = PictureFetcher.imageCache.get(fi.url) match { 
          case Some(imageIcon) => {
            imageIcon
          }
          case None => {
            val newIcon = new ImageIcon(new URL(fi.url))
            if (PictureFetcher.imageCache.size > 1000) PictureFetcher.imageCache.clear // TODO clear LRU instead?
            PictureFetcher.imageCache(fi.url) = newIcon
            newIcon
          }
        }
        SwingInvoke.invokeLater({processFinishedImage(new ImageReady(fi.url, fi.id, icon))})
      } // else ignore this request because of the newer one behind it
  }

  start
}