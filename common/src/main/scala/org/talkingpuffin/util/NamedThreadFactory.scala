package org.talkingpuffin.util
import java.util.concurrent.{ThreadFactory, Executors}

/**
 * A wrapper for the default Executors thread factor that replaces the pool-n-thread-n
 * thread names with names in the form “baseName n”.
 */
class NamedThreadFactory(baseName: String) extends ThreadFactory {
  val factory = Executors.defaultThreadFactory

  def newThread(p1: Runnable) = {
    val thread = factory.newThread(p1)
    val id = thread.getName.split("-").toList.last
    thread.setName(baseName + " " + id)
    thread
  }
}
