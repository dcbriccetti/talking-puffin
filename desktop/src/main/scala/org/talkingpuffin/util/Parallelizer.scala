package org.talkingpuffin.util

import java.util.concurrent.{Callable, Executors}

object Parallelizer {
  /**
   * Runs, in the number of threads requested, the function f, giving it each A of args, returning a List[T]
   */
  def run[T,A,F](numThreads: Int, args: List[A], f: (A) => T): List[T] = {
    val pool = Executors.newFixedThreadPool(numThreads)
    val result: List[T] = (for {
      arg <- args
      future = pool.submit(new Callable[T] {def call = f(arg)})
    } yield future).map(_.get)
    pool.shutdown
    result
  }
}