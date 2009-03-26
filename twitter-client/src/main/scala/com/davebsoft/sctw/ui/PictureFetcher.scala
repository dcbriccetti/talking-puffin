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
  val thumbnailCache = scala.collection.mutable.Map.empty[String, ImageIcon]
}

case class FetchImage(val url: String, val id: Object)
class ImageReady(val url: String, val id: Object, val imageIcon: ImageIcon)
  
class PictureFetcher(processFinishedImage: (ImageReady) => Unit, processAll: Boolean) extends Actor {
  
  def act = while(true) receive {
    case fetchImage: FetchImage =>
      if (mailboxSize == 0 || processAll) {
        val icon = PictureFetcher.thumbnailCache.get(fetchImage.url) match { 
          case Some(imageIcon) => {
            imageIcon
          }
          case None => {
            val newIcon = new ImageIcon(new URL(fetchImage.url))
            if (PictureFetcher.thumbnailCache.size > 1000) PictureFetcher.thumbnailCache.clear // TODO clear LRU instead?
            PictureFetcher.thumbnailCache(fetchImage.url) = newIcon
            newIcon
          }
        }
        SwingInvoke.invokeLater({processFinishedImage(new ImageReady(fetchImage.url, fetchImage.id, icon))})
      } // else ignore this request because of the newer one behind it
  }

  start
}