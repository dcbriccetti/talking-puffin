package org.talkingpuffin.ui

import javax.swing.{JInternalFrame, JDesktopPane}
import java.awt.{Toolkit, Dimension}
import org.talkingpuffin.Session

/**
 * This JDesktopPane sits in the TopFrame and manages several JInternalFrames.
 */
class DesktopPane(session: Session) extends JDesktopPane {
  val tweetDetailPanel = new TweetDetailPanel(session, None)
  setDragMode(JDesktopPane.OUTLINE_DRAG_MODE)

  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  private val desktopSize = new Dimension(math.min(screenSize.width * 4 / 5, 1200), screenSize.height * 4 / 5)

  setPreferredSize(desktopSize)

  add(new JInternalFrame("Status Details", true, false, false, true) {
    setLayer(10)
    setContentPane(tweetDetailPanel.peer)
    pack
    setLocation(desktopSize.width / 2 - tweetDetailPanel.preferredSize.width / 2,
      desktopSize.height / 2 - tweetDetailPanel.preferredSize.height / 2)
    setVisible(true)
  })
}