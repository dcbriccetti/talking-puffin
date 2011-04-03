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
object PictureFetcher extends Loggable {
  type ImageReady = ResourceReady[ImageWithScaled]

  /** Derives the full size filename from the thumbnail filename */
  def getFullSizeUrl(thumb: String) = Picture.getFullSizeUrl(thumb)

  private def scaleImageToFitSquare(sideLength: Int, imageIcon: ImageIcon): ImageIcon = {
    val image = imageIcon.getImage
    val w = image.getWidth(null)
    val h = image.getHeight(null)
    if (w <= sideLength && h <= sideLength)
      imageIcon
    else {
      val newW: Int = if (w > h) math.min(w, sideLength) else -1
      val newH: Int = if (w > h) -1 else math.min(h, sideLength)
      val scaledImage = tlog(debug, "Scale " + w + " x " + h + " " + imageIcon, {
        new ImageIcon(image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH))
      })
      scaledImage
    }
  }
}

/**
 * A picture fetcher, which when instantiated with an optional scale maximum and a “done” callback,
 * can be called with its requestItem method to request pictures.
 */
class PictureFetcher(resource: String, scaleTo: Option[Int], numThreads: Int)
  extends BackgroundResourceFetcher[ImageWithScaled](resource, numThreads = numThreads) with Loggable {
  
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

case class FetchImageRequest(url: String, id: Object, processFinishedImage: (PictureFetcher.ImageReady) => Unit)
  extends FetchRequest[ImageWithScaled](url, id, processFinishedImage)
