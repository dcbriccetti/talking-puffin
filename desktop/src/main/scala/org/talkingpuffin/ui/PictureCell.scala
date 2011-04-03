package org.talkingpuffin.ui

import javax.swing.table.{AbstractTableModel}
import javax.swing.{Icon}
import akka.actor._
import akka.actor.Actor._
import PictureFetcher.ImageReady
import org.talkingpuffin.util.FetchRequest

/**
 * A JTable cell with an asynchronously-loaded image in it.
 */

object picFetcher extends PictureFetcher("Picture cell", None, 5, Some(100))

class PictureCell(model: AbstractTableModel, column: Int) {

  val picFetchActor = actorOf(new Actor() {

    protected def receive = {
      case request: FetchRequest => picFetcher.requestItem(request)

      case imageReady: ImageReady => SwingInvoke.later {
        if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
          val row = imageReady.request.userData.asInstanceOf[Int]
          model.fireTableCellUpdated(row, column)
        }
      }
    }
  }).start()

  def request(picUrl: String, rowIndex: Int): Icon = {
    picFetcher.getCachedObject(picUrl) match {
      case Some(imageWithScaled) => imageWithScaled.image
      case None =>
        picFetchActor ! FetchRequest(picUrl, rowIndex.asInstanceOf[Object], picFetchActor)
        Thumbnail.transparentThumbnail
    }
  }
  
}
