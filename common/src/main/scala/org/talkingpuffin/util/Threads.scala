package org.talkingpuffin.util

import java.util.concurrent.{Callable, Executors}

object Threads {
  val pool = Executors.newCachedThreadPool(NamedThreadFactory("App"))

  def callable[T](fn: => T) = new Callable[T] {
    def call = fn
  }

  def submitCallable[T](fn: => T) = pool.submit(callable(fn))
}
