package org.talkingpuffin.ui

import org.talkingpuffin.Session

trait ActionProcessor {
  val session: Session
  
  def process[T](names:List[T], action:((T) => Unit), actionName: String, msg: String) = 
    names foreach {name => 
      try {
        action(name)
        session.addMessage(String.format(msg, name))
      } catch {
        case e: Throwable => showActionErr(e, actionName, name)
      }
    }

  private def showActionErr(e: Throwable, actionName: String, screenName: String) =
    session.addMessage("Error " + actionName + " " + screenName)
  
}