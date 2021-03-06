package org.talkingpuffin.ui

import twitter4j.User
import org.talkingpuffin.Session
import org.talkingpuffin.util.Loggable

trait ActionProcessor extends Loggable {
  val session: Session
  
  def process[T](items: Seq[T], action: ((T) => Unit), actionName: String, msg: String) {
    items.foreach(item =>
      session.addMessage(
        try {
          action(item)
          msg.format(item)
        } catch {
          case e: Throwable =>
            error(e.getMessage)
            "Error " + actionName + " " + item.toString
        }
      )
    )
  }

  def processUsers(items: Seq[String], action: (String => User), actionName: String, msg: String) =
    process(items, (name: String) => {action(name)}: Unit, actionName, msg)
}
