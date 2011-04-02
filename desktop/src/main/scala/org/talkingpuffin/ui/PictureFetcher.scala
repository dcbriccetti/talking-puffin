package org.talkingpuffin.ui

import javax.swing.{ImageIcon}
import java.net.URL
import org.talkingpuffin.util._
import org.talkingpuffin.util.TimeLogger.{run => tlog}
import java.io.Serializable
import java.awt.{MediaTracker, Image}

/**
 * Fetches pictures in the background, and calls a method in the event
 * dispatching thread when done.
 */
object PictureFetcher {
  type ImageReady = ResourceReady[String,ImageWithScaled]

  /** Derives the full size filename from the thumbnail filename */
  def getFullSizeUrl(thumb: String) = Picture.getFullSizeUrl(thumb)

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
class PictureFetcher(resource: String, scaleTo: Option[Int])
  extends BackgroundResourceFetcher[ImageWithScaled](resource) with Loggable {
  
  def fetchImageRequest(url: String, id: Object, processFinishedImage: (PictureFetcher.ImageReady) => Unit) =
      new FetchRequest[String,ImageWithScaled](url, id, processFinishedImage)

  /**
   * Given the URL provided, fetches an image, and if the PictureFetcher was created with a scaleTo value,
   * uses that size to produce a scaled version of the image.
   */
  protected def getResourceFromSource(url: String): ImageWithScaled = {
    val icon = tlog(debug, "Create ImageIcon for " + url, {new ImageIcon(new URL(url))})
    if (icon.getImageLoadStatus != MediaTracker.COMPLETE) {
      warn("Could not load image for " + url)
      throw new NoSuchResource(url)
    }
    ImageWithScaled(icon, scaleTo.map(sideLength => PictureFetcher.scaleImageToFitSquare(sideLength, icon)))
  }

}

case class ImageWithScaled(image: ImageIcon, scaledImage: Option[ImageIcon]) extends Serializable
