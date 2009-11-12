package org.talkingpuffin.ui.util

import swing.{Label, BorderPanel}
import java.awt.Dimension
import org.talkingpuffin.ui.Thumbnail

/**
 * Centers pictures of different shapes.
 */
class CenteredPicture(picLabel: Label) extends BorderPanel {
  val s = new Dimension(Thumbnail.MEDIUM_SIZE + 6, Thumbnail.MEDIUM_SIZE + 6)
  minimumSize = s
  maximumSize = s
  preferredSize = s
  add(picLabel, BorderPanel.Position.Center)
}

