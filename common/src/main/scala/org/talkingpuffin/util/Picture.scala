package org.talkingpuffin.util

object Picture {
  /** Derives the full size filename from the thumbnail filename */
  def getFullSizeUrl(thumb: String) = thumb.replace("_normal", "")
}