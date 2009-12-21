package org.talkingpuffin.ui

import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Session

trait ErrorHandler extends Loggable {
  def doAndHandleError(f: () => Unit, msg: String, session: Session) {
    try {
      f()
    } catch {
      case e: Exception => {
        val displayMsg = msg + ": " + e.getMessage
        error(displayMsg)
        session.addMessage(displayMsg)
      }
    }
    
  }
}