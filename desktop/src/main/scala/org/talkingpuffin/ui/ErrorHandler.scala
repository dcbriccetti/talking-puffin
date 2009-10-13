package org.talkingpuffin.ui

import javax.swing.JOptionPane
import org.talkingpuffin.util.Loggable

trait ErrorHandler extends Loggable {
  def doAndHandleError(f: () => Unit, msg: String) {
    try {
      f()
    } catch {
      case e: Throwable => {
        error(msg + ": " + e.getMessage)
        JOptionPane.showMessageDialog(null, msg)
      }
    }
    
  }
}