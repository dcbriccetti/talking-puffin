package org.talkingpuffin.ui.util

import java.awt.Component
import javax.swing.{JInternalFrame, SwingUtilities, JFrame}

/**
 * A descendant of a JInternalFrame or JFrame that can be queried to see if it is active, 
 * or set active.
 */
trait Activateable extends Component {

  /**
   * Returns whether the JInternalFrame/JFrame ancestor of this Component is selected/active.
   */
  def isActive: Boolean = doWithAncestor((intFrame) => intFrame.isSelected(), 
                                         (jframe)   => jframe.isActive())

  /**
   * Selects/requests focus in the JInternalFrame/JFrame ancestor of this Component. 
   */
  def activate = doWithAncestor((intFrame) => intFrame.setSelected(true), 
                                (jframe)   => jframe.requestFocusInWindow())

  /**
   * Calls doForInternalFrame if the ancestor is a JInternalFrame, and doForJFrame if
   * it is a JFrame.
   */
  private def doWithAncestor[T](doForInternalFrame: (JInternalFrame) => T, 
                                doForJFrame: (JFrame) => T): T = {
    SwingUtilities.getAncestorOfClass(classOf[JInternalFrame], this) match {
      case f: JInternalFrame => doForInternalFrame(f)
      case _ => SwingUtilities.getAncestorOfClass(classOf[JFrame], this) match {
        case f: JFrame => doForJFrame(f)
      }
    } 
  }
}

