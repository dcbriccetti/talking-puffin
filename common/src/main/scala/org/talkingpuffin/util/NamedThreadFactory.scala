package org.talkingpuffin.util
import java.util.concurrent.{ThreadFactory, Executors}
import java.util.concurrent.atomic.AtomicLong

/**
 * A wrapper for the default Executors thread factor that replaces the pool-n-thread-n
 * thread names with names in the form “baseName n”.
 */
class NamedThreadFactory(baseName: String) extends ThreadFactory {
  val factory = Executors.defaultThreadFactory
  val id = new AtomicLong(0)

  def newThread(p1: Runnable) = {
    val thread = factory.newThread(p1)
    thread.setName(baseName + " " + id.incrementAndGet)
    thread
  }
}
