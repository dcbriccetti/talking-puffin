package org.talkingpuffin.util

import scala.collection.JavaConversions._
import java.util.concurrent.{ExecutorCompletionService, Callable, Executors}
import java.util.{ArrayList, Collections}
import java.text.NumberFormat

object Parallelizer extends Loggable {
  /**
   * Runs, in the number of threads requested, the function f, giving it each A of args, returning a List[T]
   */
  def run[T,A](numThreads: Int, args: Seq[A], f: (A) => T): List[T] = {
    val timings = Collections.synchronizedList(new ArrayList[Long])
    val pool = Executors.newFixedThreadPool(numThreads)
    val completionService = new ExecutorCompletionService[T](pool)
    args.foreach(arg => completionService.submit(new Callable[T] {
      def call = {
        val startTime = System.currentTimeMillis
        val result = f(arg)
        timings.add(System.currentTimeMillis - startTime)
        result
      }
    }))
    val result = args map(_ => completionService.take.get)
    pool.shutdown
    logStats(timings)
    result.toList
  }

  private def calcStdDev(timings: java.util.List[Long], mean: Double): Double = {
    val difSq = timings.map(timing => {
      val dif = timing - mean
      dif * dif
    })
    math.sqrt(difSq.sum / timings.size)
  }

  private def logStats[A, T](timings: java.util.List[Long]): Unit = {
    val fmt = NumberFormat.getInstance
    val mean = timings.sum.toDouble / timings.size
    debug(timings.sorted.map(timing => fmt.format(timing)).toList.mkString(", "))
    debug("Mean: " + fmt.format(mean) + ", Std dev: " + fmt.format(calcStdDev(timings, mean)))
  }

}
