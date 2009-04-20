package com.davebsoft.sctw.ui

import _root_.scala.actors.Actor
import cache.Cache
import google.common.collect.MapMaker
import java.awt.Image
import java.util.{Collections, HashSet}
import javax.swing.{Icon, ImageIcon}
import scala.actors.Actor._
import java.net.URL
import util.{FetchRequest, ResourceReady, BackgroundResourceFetcher}
/**
 * Fetches pictures in the background, and calls a method in the event
 * dispatching thread when done.
 * @author Dave Briccetti
 */

object PictureFetcher {
  type ImageReady = ResourceReady[String,ImageWithScaled]

  /** Derives the full size filename from the thumbnail filename */
  def getFullSizeUrl(thumb: String): String = thumb.replace("_normal", "")

  private def scaleImageToFitSquare(sideLength: Int, imageIcon: ImageIcon): ImageIcon = {
    val image = imageIcon.getImage
    val w = image.getWidth(null)
    val h = image.getHeight(null)
    val newW: Int = if (w > h) Math.min(w, sideLength) else -1
    val newH: Int = if (w > h) -1 else Math.min(h, sideLength)
    new ImageIcon(image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH))
  }
}

/**
 * A picture fetcher, which when instantiated with an optional scale maximum and a “done” callback,
 * can be called with its requestItem method to request pictures.
 */
class PictureFetcher(scaleTo: Option[Int], processFinishedImage: (PictureFetcher.ImageReady) => Unit) 
    extends BackgroundResourceFetcher[String, ImageWithScaled](processFinishedImage) 
    with Cache[String,ImageWithScaled] {
  
  def FetchImageRequest(url: String, id: Object) = new FetchRequest[String](url, id)

  protected def getResourceFromSource(url: String): ImageWithScaled = {
    val icon = new ImageIcon(new URL(url))
    ImageWithScaled(icon,
      scaleTo match {
        case Some(sideLength) => Some(PictureFetcher.scaleImageToFitSquare(sideLength, icon))
        case None => None
      })
  }

}

case class ImageWithScaled(val image: ImageIcon, val scaledImage: Option[ImageIcon])
