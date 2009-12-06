package org.talkingpuffin.util

import java.util.concurrent.{ExecutorCompletionService, Callable, Executors}

object Parallelizer {
  /**
   * Runs, in the number of threads requested, the function f, giving it each A of args, returning a List[T]
   */
  def run[T,A](numThreads: Int, args: Seq[A], f: (A) => T): List[T] = {
    val pool = Executors.newFixedThreadPool(numThreads)
    val completionService = new ExecutorCompletionService[T](pool)
    args.foreach(arg => completionService.submit(new Callable[T] {def call = f(arg)}))
    val result = args map(_ => completionService.take.get)
    pool.shutdown
    result.toList
  }
}
