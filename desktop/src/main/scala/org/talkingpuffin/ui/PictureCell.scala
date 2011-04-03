package org.talkingpuffin.ui

import javax.swing.table.{AbstractTableModel}
import javax.swing.{Icon}

/**
 * A JTable cell with an asynchronously-loaded image in it.
 */

object picFetcher extends PictureFetcher("Picture cell", None, 5, Some(100))

class PictureCell(model: AbstractTableModel, column: Int) {
    
  def request(picUrl: String, rowIndex: Int): Icon = {
    picFetcher.getCachedObject(picUrl) match {
      case Some(imageWithScaled) => imageWithScaled.image
      case None => {
        picFetcher.requestItem(FetchImageRequest(picUrl, rowIndex.asInstanceOf[Object],
          (imageReady: PictureFetcher.ImageReady) => SwingInvoke.later {
            if (imageReady.resource.image.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
              val row = imageReady.userData.asInstanceOf[Int]
              model.fireTableCellUpdated(row, column)
            }
          }))
        Thumbnail.transparentThumbnail
    }
  }
  }
  
}
