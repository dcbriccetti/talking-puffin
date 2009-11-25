package org.talkingpuffin.ui.util

import java.awt.Component
import javax.swing.{JInternalFrame, SwingUtilities, JFrame}

/**
 * A descendant of a JFrame or JInternalFrame that can be queried to see if it is active.
 */
trait Activateable extends Component {
  def isActive: Boolean = {
    SwingUtilities.getAncestorOfClass(classOf[JInternalFrame], this) match {
      case f: JInternalFrame => f.isSelected()
      case _ => SwingUtilities.getAncestorOfClass(classOf[JFrame], this) match {
        case jf: JFrame => jf.isActive()
        case null => false
      }
    } 
  }
  
}