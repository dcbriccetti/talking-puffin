package org.talkingpuffin.ui.util

import swing.TabbedPane.Page
import javax.swing.{JFrame, SwingUtilities, JComponent, JToggleButton}
import swing.{Action, Frame, TabbedPane, Component}
import org.talkingpuffin.Session
import org.talkingpuffin.ui.{MainMenuBar}
import org.talkingpuffin.util.Loggable
import java.awt.{Point, Dimension}

/**
 * Provides a “docked” toggle button, connected to undock and dock methods.
 */
trait Dockable extends Component with Loggable {
  val session: Session
  val longTitle: String
  val shortTitle: String
  
  private var titleSuffix_ = ""
  def titleSuffix = titleSuffix_
  private val tabbedPane = session.windows.tabbedPane
  
  val dockedButton: JToggleButton = new JToggleButton(new Action("Docked") {
    toolTip = "Docks or frees the pane"
    def apply = if (dockedButton.isSelected) dock else undock(None)
  }.peer) {setSelected(true)} 

  def undock(loc: Option[Point]): Frame = {
    peer.asInstanceOf[JComponent].setPreferredSize(new Dimension(800,700))
    val frame = new Frame {
      title = withSuffix(longTitle)
      contents = Dockable.this
      menuBar = new MainMenuBar(session.windows.streams.providers, session.windows.streams.tagUsers)
      loc match {case Some(l) => location = l case None => peer.setLocationRelativeTo(null)}
    }
    frame.visible = true
    frame
  }
  
  private def dock {                                            
    val frame = SwingUtilities.getAncestorOfClass(classOf[JFrame], peer).asInstanceOf[JFrame]
    val newPage = new Page(withSuffix(shortTitle), this) {tip = longTitle}
    tabbedPane.pages += newPage
    tabbedPane.selection.page = newPage 
    frame.dispose
  }

  def titleSuffix_=(newTitleSuffix: String) = {
    titleSuffix_ = newTitleSuffix
    tabbedPane.peer.indexOfComponent(peer) match {
      case -1 => 
        SwingUtilities.getAncestorOfClass(classOf[JFrame], peer) match {
          case null =>
          case jf => jf.asInstanceOf[JFrame].setTitle(withSuffix(longTitle)) 
        }
      case i => tabbedPane.peer.setTitleAt(i, withSuffix(shortTitle))
    }
  }
  
  private def withSuffix(title: String) = if (titleSuffix_.length == 0) title else title + " " + titleSuffix_
}
  