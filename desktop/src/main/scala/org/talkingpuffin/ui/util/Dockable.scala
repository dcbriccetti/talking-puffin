package org.talkingpuffin.ui.util

import swing.{Action}
import javax.swing.JToggleButton
import _root_.scala.swing.{Frame, TabbedPane, Component}
import javax.swing.{JTabbedPane, JFrame, SwingUtilities}
import org.talkingpuffin.Session
import org.talkingpuffin.ui.{MainMenuBar}
import org.talkingpuffin.util.Loggable

/**
 * Provides a “docked” toggle button, connected to undock and dock methods.
 */
trait Dockable extends Component with Loggable {
  val session: Session
  val paneTitle: String
  
  var dockedButton: JToggleButton = _ 
  val dockedAction = new Action("Docked") {
    toolTip = "Docks or frees the pane"
    def apply = if (dockedButton.isSelected) dock else undock
  }
  dockedButton = new JToggleButton(dockedAction.peer)
  dockedButton.setSelected(true)

  private def undock {
    new Frame {
      title = paneTitle + " " + (peer.getParent match {
        case tp: JTabbedPane => tp.getTitleAt(tp.indexOfComponent(peer))
        case _ => ""
      })
      contents = Dockable.this
      menuBar = new MainMenuBar(session.windows.streams.providers)
    }.visible = true
  }
  
  private def dock {                                            
    val frame = SwingUtilities.getAncestorOfClass(classOf[JFrame], peer).asInstanceOf[JFrame]
    session.windows.tabbedPane.pages += new TabbedPane.Page(frame.getTitle, this) {tip = paneTitle}
    frame.dispose
  }

}
  
