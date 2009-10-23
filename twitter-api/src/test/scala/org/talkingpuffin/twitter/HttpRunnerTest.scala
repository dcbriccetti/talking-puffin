package org.talkingpuffin.twitter

import org.specs.runner.JUnit4
import org.specs.Specification
import org.apache.log4j.BasicConfigurator

class HttpRunnerTest extends JUnit4(HttpRunnerSpec)
 
object HttpRunnerSpec extends Specification {
  BasicConfigurator.configure
    
  "Runner retries and calculates correct statistics" in {
    val retryAfterFailureDelays = List(0, 10)
    val runner = new HttpRunner(retryAfterFailureDelays)
    var runs = 0
    def f = {
      runs += 1
      if (runs == 1)
      throw new TwitterException(500, "Over capacity")
    }
    runner.run(f)
    runner.getWhenSucceeded(0) must_== 0
    runner.getWhenSucceeded(1) must_== 1
    runner.getWhenSucceeded(2) must_== 0
  }
}
 
 