package org.talkingpuffin.util

import java.util.concurrent.{Callable, Executors}

object Parallelizer {
  def run[T,A,F](numThreads: Int, args: List[A], f: (A) => T) = {
    val pool = Executors.newFixedThreadPool(numThreads)
    (for {
      arg <- args
      future = pool.submit(new Callable[T] {def call = f(arg)})
    } yield future).map(_.get)
  }
}