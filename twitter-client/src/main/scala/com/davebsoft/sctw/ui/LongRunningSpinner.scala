package com.davebsoft.sctw.ui

import java.awt.{Frame, Cursor}
import SwingInvoke._

/**
 * Whenever a long running operation is performed the UI thread should be released and a spinner should be shown. This object has
 * functions for simplyfying this tasks. Every functions spins of a thread, and calls a callback function when job is finished.
 * @author Alf Kristian Støyle
 */
object LongRunningSpinner {

  /**
   * Handles a single functions and its return value. Calls the callback (if not null) when the function has finished.
   */
  def run[T](frame: HasCursor, callback: (T) => Unit, f: => T) {
    executeThread{
      invokeLater(frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)))
      val ret = f
      invokeLater(frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)))
      if(callback != null) callback(ret)
    }
  }
  
  /**
   * Handles several functions after each other on the same thread, and the function returns true if the next function should be
   * executed, false otherwise. The callback funtion is called when the functions have finished
   */
  def run(frame: HasCursor, callback: () => Unit, functions: () => Boolean*) {
    executeThread {
      invokeLater(frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)))
      functions.find( !_() )
      invokeLater(frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)))
    }
    if(callback != null) callback()
  }

  private def executeThread(f: => Unit) {
    new Thread{ override def run{f} }.start
  }
  
  trait HasCursor {
    def setCursor(c: Cursor)
  }
  
  implicit def toHasCursor[T <: { def setCursor(c: Cursor) }](t: T) = new HasCursor { def setCursor(c: Cursor) = t.setCursor(c) } 
  
}
