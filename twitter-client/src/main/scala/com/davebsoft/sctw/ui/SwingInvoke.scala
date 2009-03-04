package com.davebsoft.sctw.ui

import javax.swing.SwingUtilities

/**
 * Simplifies calling the EventDispatchThread
 * @author Alf Kristian Støyle
 */
object SwingInvoke {
  
  def invokeLater(f: => Unit) {
    SwingUtilities.invokeLater(new Runnable{ def run{f} })
  }

}
