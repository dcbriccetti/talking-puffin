package org.talkingpuffin.ui

import java.awt.{Toolkit, Dimension}
import org.talkingpuffin.Session
import javax.swing.{JTabbedPane, JComponent, JInternalFrame, JDesktopPane}

/**
 * This JDesktopPane sits in the TopFrame and manages several JInternalFrames.
 */
class DesktopPane(val session: Session) extends JDesktopPane with MainContents {
  setDragMode(JDesktopPane.OUTLINE_DRAG_MODE)
  setSize()

  add(new JInternalFrame("Status Details", true, false, false, true) {
    setLayer(10)
    setContentPane(tweetDetailPanel.peer)
    pack
    setLocation(desktopSize.width / 2 - tweetDetailPanel.preferredSize.width / 2,
      desktopSize.height / 2 - tweetDetailPanel.preferredSize.height / 2)
    setVisible(true)
  })
}

class TabbedPane(val session: Session) extends JTabbedPane with MainContents {
  setSize()
}

trait MainContents extends JComponent {
  val session: Session
  val tweetDetailPanel = new TweetDetailPanel(session, None)
  val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val desktopSize = new Dimension(math.min(screenSize.width * 4 / 5, 1200), screenSize.height * 4 / 5)

  def setSize() = setPreferredSize(desktopSize)
}