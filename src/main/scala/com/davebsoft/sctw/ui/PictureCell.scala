package com.davebsoft.sctw.ui

import javax.swing.Icon
import javax.swing.table.{AbstractTableModel}

/**
 * A JTable cell with an asynchronously-loaded image in it.
 * @author Dave Briccetti
 */

class PictureCell(model: AbstractTableModel, column: Int) {
  val picFetcher = new PictureFetcher(None, (imageReady: ImageReady) => {
    if (imageReady.imageIcon.getIconHeight <= Thumbnail.THUMBNAIL_SIZE) {
      val row = imageReady.id.asInstanceOf[Int]
      model.fireTableCellUpdated(row, column)
    }
  }, true)
    
  def request(picUrl: String, rowIndex: Int): Icon = {
    val imageIcon = PictureFetcher.pictureCache.get(picUrl)
    if (imageIcon != null) imageIcon else {
      picFetcher.requestImage (picUrl, rowIndex.asInstanceOf[Object])
      Thumbnail.transparentThumbnail
    }
  }
}
