package com.davebsoft.sctw.ui

import _root_.scala.actors.Actor
import java.awt.Image
import scala.actors.Actor._
import java.net.URL
import javax.swing.ImageIcon

/**
 * Fetches pictures in the background, and calls a method in the event
 * dispatching thread when done.
 * @author Dave Briccetti
 */

object PictureFetcher {
  val pictureCache = scala.collection.mutable.Map.empty[String, ImageIcon]
  val scaledPictureCache = scala.collection.mutable.Map.empty[String, ImageIcon]
  
  def getFullSizeUrl(thumb: String): String = thumb.replace("_normal", "")

  private def scaleImage(limit: Int, imageIcon: ImageIcon): ImageIcon = {
    val image = imageIcon.getImage
    val w = image.getWidth(null)
    val h = image.getHeight(null)
    val newW: Int = if (w > h) Math.min(w, limit) else -1
    val newH: Int = if (w > h) -1 else Math.min(h, limit)
    new ImageIcon(image.getScaledInstance(newW, newH, Image.SCALE_DEFAULT))
  }

}

case class FetchImage(val url: String, val id: Object)
class ImageReady(val url: String, val id: Object, val imageIcon: ImageIcon)
  
class PictureFetcher(scaleTo: Option[Int], 
    processFinishedImage: (ImageReady) => Unit, processAll: Boolean) extends Actor {
  
  def act = while(true) receive {
    case fetchImage: FetchImage =>
      if (mailboxSize == 0 || processAll) {
        var icon = PictureFetcher.pictureCache.get(fetchImage.url) match { 
          case Some(imageIcon) => {
            imageIcon
          }
          case None => {
            println("Fetching " + fetchImage.url)
            val newIcon = new ImageIcon(new URL(fetchImage.url))
            if (PictureFetcher.pictureCache.size > 1000) PictureFetcher.pictureCache.clear // TODO clear LRU instead?
            PictureFetcher.pictureCache(fetchImage.url) = newIcon
            newIcon
          }
        }
        scaleTo match {
          case Some(limit) => {
            icon = PictureFetcher.scaleImage(limit, icon)
            if (PictureFetcher.scaledPictureCache.size > 1000) PictureFetcher.scaledPictureCache.clear // TODO clear LRU instead?
            PictureFetcher.scaledPictureCache(fetchImage.url) = icon
          }
          case None =>
        }
        SwingInvoke.invokeLater({processFinishedImage(new ImageReady(fetchImage.url, fetchImage.id, icon))})
      } // else ignore this request because of the newer one behind it
  }

  start
}