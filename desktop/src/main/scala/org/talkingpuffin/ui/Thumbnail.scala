package org.talkingpuffin.ui

import javax.swing.ImageIcon
import java.awt.image.BufferedImage

object Thumbnail {
  val THUMBNAIL_SIZE = 48
  val transparentThumbnail = new ImageIcon(new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, 
    BufferedImage.TYPE_INT_ARGB))

  val MEDIUM_SIZE = 220
  val transparentMedium = new ImageIcon(new BufferedImage(MEDIUM_SIZE, MEDIUM_SIZE, 
    BufferedImage.TYPE_INT_ARGB))
}

