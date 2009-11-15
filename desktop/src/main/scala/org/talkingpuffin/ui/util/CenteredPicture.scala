package org.talkingpuffin.ui.util

import swing.{Label, BorderPanel}
import org.talkingpuffin.ui.Thumbnail
import java.awt.{Color, Dimension}

/**
 * Centers pictures of different shapes.
 */
class CenteredPicture(picLabel: Label) extends BorderPanel {
  val s = new Dimension(Thumbnail.MEDIUM_SIZE, Thumbnail.MEDIUM_SIZE)
  minimumSize = s
  maximumSize = s
  preferredSize = s
  background = Color.WHITE
  add(picLabel, BorderPanel.Position.Center)
}

