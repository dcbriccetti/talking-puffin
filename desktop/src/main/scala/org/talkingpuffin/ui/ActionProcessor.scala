package org.talkingpuffin.ui

import org.talkingpuffin.Session

trait ActionProcessor {
  val session: Session
  
  def process[T <: Object](items: Seq[T], action: ((T) => Unit), actionName: String, msg: String) =
    items.foreach(item =>
      session.addMessage( 
        try {
          action(item)
          String.format(msg, item)
        } catch {
          case e: Throwable => "Error " + actionName + " " + item.toString
        }
      )
    )
  
}