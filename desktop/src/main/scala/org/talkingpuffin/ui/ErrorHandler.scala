package org.talkingpuffin.ui

import javax.swing.JOptionPane
import org.talkingpuffin.util.Loggable

trait ErrorHandler extends Loggable {
  def doAndHandleError(f: () => Unit, msg: String) {
    try {
      f()
    } catch {
      case e: Throwable => {
        val displayMsg = msg + ": " + e.getMessage
        error(displayMsg)
        JOptionPane.showMessageDialog(null, displayMsg)
      }
    }
    
  }
}