package org.talkingpuffin.ui.util

import swing.{Action}
import javax.swing.JToggleButton
import _root_.scala.swing.{Frame, TabbedPane, Component}
import javax.swing.{JTabbedPane, JFrame, SwingUtilities}
import org.talkingpuffin.Session
import org.talkingpuffin.ui.{MainMenuBar}

/**
 * Provides a “docked” toggle button, connected to undock and dock methods.
 */
trait Dockable {
  val session: Session
  val pane: Component
  
  var dockedButton: JToggleButton = _ 
  val dockedAction = new Action("Docked") {
    toolTip = "Docks or frees the pane"
    def apply = if (dockedButton.isSelected) dock(pane) else undock(pane)
  }
  dockedButton = new JToggleButton(dockedAction.peer)
  dockedButton.setSelected(true)

  private def undock(comp: Component) {
    new Frame {
      title = comp.peer.getParent match {
        case tp: JTabbedPane => tp.getTitleAt(tp.indexOfComponent(comp.peer))
        case _ => null
      }
      contents = comp
      menuBar = new MainMenuBar(session.windows.streams.providers)
    }.visible = true
  }
  
  private def dock(comp: Component) {
    val frame = SwingUtilities.getAncestorOfClass(classOf[JFrame], comp.peer).asInstanceOf[JFrame]
    session.windows.tabbedPane.pages += new TabbedPane.Page(frame.getTitle, comp)
    frame.dispose
  }

}
  
