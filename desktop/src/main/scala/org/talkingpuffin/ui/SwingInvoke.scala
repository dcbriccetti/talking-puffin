package org.talkingpuffin.ui

import javax.swing.{SwingUtilities, SwingWorker}

/**
 * Simplifies calling the EventDispatchThread
 */
object SwingInvoke {
  
  def later(f: => Unit) {
    SwingUtilities.invokeLater(new Runnable{ def run{f} })
  }
  
  def execSwingWorker[T,V](inBackGround: => T, whenDone: (T) => Unit){
    new SwingWorker[T, V] {
      override def doInBackground = inBackGround
      override def done = whenDone(get)
    }.execute
  }

}
