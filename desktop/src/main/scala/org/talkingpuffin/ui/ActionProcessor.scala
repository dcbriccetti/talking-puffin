package org.talkingpuffin.ui

import org.talkingpuffin.Session

trait ActionProcessor {
  val session: Session
  
  def process[T <: Object](items: Seq[T], action: ((T) => Unit), actionName: String, msg: String) =
    items.foreach(item =>   
      try {
        action(item)
        session.addMessage(String.format(msg, item))
      } catch {
        case e: Throwable => showActionErr(e, actionName, item)
      }
    )

  private def showActionErr[T](e: Throwable, actionName: String, item: T) =
    session.addMessage("Error " + actionName + " " + item.toString)
  
}