package org.talkingpuffin.ui

import java.awt.{Toolkit, Dimension}
import org.talkingpuffin.Session
import javax.swing.{JComponent, JInternalFrame, JDesktopPane}
import swing.TabbedPane

/**
 * This JDesktopPane sits in the TopFrame and manages several JInternalFrames.
 */
class DesktopPane(val session: Session) extends JDesktopPane with MainContents {
  setDragMode(JDesktopPane.OUTLINE_DRAG_MODE)
  val screenSize = Toolkit.getDefaultToolkit.getScreenSize
  val desktopSize = new Dimension(math.min(screenSize.width * 4 / 5, 1200), screenSize.height * 4 / 5)

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

class TopTabbedPane(val session: Session) extends TabbedPane with MainContents {
  override def size = new Dimension(0,0) // todo not used, just for interface
  override def preferredSize = new Dimension(0,0) // todo not used, just for interface
}

trait MainContents {
  val session: Session
  val tweetDetailPanel = new TweetDetailPanel(session, None)
  def size: Dimension
  def preferredSize: Dimension
}