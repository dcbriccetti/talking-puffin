package org.talkingpuffin.util

import org.talkingpuffin.ui.SwingInvoke
import akka.actor.Actor
import akka.actor.Actor._

object ActorUtil {
  def swingResourceReady(fn: String => Unit) = actorOf(new Actor() {
    def receive = {
      case resourceReady: ResourceReady[String] => SwingInvoke.later {
        fn(resourceReady.resource)
      }
    }
  }).start()

}
