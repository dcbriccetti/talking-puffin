package com.davebsoft.sctw.ui

import _root_.scala.actors.Actor
import google.common.collect.MapMaker
import java.awt.Image
import java.util.concurrent.{ConcurrentHashMap, Executors, LinkedBlockingQueue}
import java.util.{Collections, HashSet}
import javax.swing.{Icon, ImageIcon}
import scala.actors.Actor._
import java.net.URL
/**
 * Fetches pictures in the background, and calls a method in the event
 * dispatching thread when done.
 * @author Dave Briccetti
 */

object PictureFetcher {
  val pictureCache:       java.util.Map[String, ImageIcon] = new MapMaker().softValues().makeMap()
  val scaledPictureCache: java.util.Map[String, ImageIcon] = new MapMaker().softValues().makeMap()
  
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

case class FetchImageRequest(val url: String, val id: Object)
class ImageReady(val url: String, val id: Object, val imageIcon: ImageIcon)

/**
 * A picture fetcher, which when instantiated with an optional scale maximum and a “done” callback,
 * can be called with its requestImage method to request pictures.
 */
class PictureFetcher(scaleTo: Option[Int], 
    processFinishedImage: (ImageReady) => Unit, processAll: Boolean) {
  
  val requestQueue = new LinkedBlockingQueue[FetchImageRequest]
  val inProgress = Collections.synchronizedSet(new HashSet[String])
  val pool = Executors.newFixedThreadPool(15)
  
  new Thread(new Runnable {
    def run = while(true) {
      processNextImageRequestWithPoolThread
    }
  }).start

  /**
   * Requests that an image be fetched in a background thread. If the URL is already in the 
   * cache, the request is ignored. 
   */
  def requestImage(url: String, id: Object) {
    if (! PictureFetcher.pictureCache.containsKey(url)) {
      val req = new FetchImageRequest(url, id)
      if (! requestQueue.contains(req) && ! inProgress.contains(url)) {
        requestQueue.put(req)
      }
    }
  }
  
  private def processNextImageRequestWithPoolThread {
    val fetchImage = requestQueue.take
    inProgress.add(fetchImage.url)
    pool.execute(new Runnable {
      def run = {
        var icon = getPictureFromCacheOrWeb(fetchImage.url)

        scaleTo match {
          case Some(sideLength) => {
            icon = PictureFetcher.scaleImageToFitSquare(sideLength, icon)
            if (PictureFetcher.scaledPictureCache.size > 1000) 
              PictureFetcher.scaledPictureCache.clear // TODO clear LRU instead
            PictureFetcher.scaledPictureCache.put(fetchImage.url, icon)
          }
          case None =>  
        }
            
        inProgress.remove(fetchImage.url)
        SwingInvoke.invokeLater({processFinishedImage(
          new ImageReady(fetchImage.url, fetchImage.id, icon))})
      }
    })
  }
  
  private def getPictureFromCacheOrWeb(url: String): ImageIcon = {
    var icon = PictureFetcher.pictureCache.get(url)
    if (icon == null) {
      icon = new ImageIcon(new URL(url))
      if (PictureFetcher.pictureCache.size > 1000) 
        PictureFetcher.pictureCache.clear // TODO clear LRU instead
      PictureFetcher.pictureCache.put(url, icon)
    }
    icon
  }
}