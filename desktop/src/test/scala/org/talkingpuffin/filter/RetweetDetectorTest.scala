package org.talkingpuffin.filter

import org.specs.runner.JUnit4
import org.specs.Specification
import RetweetDetector._
 
class RetweetDetectorTest extends JUnit4(RetweetDetectorSpec)
 
object RetweetDetectorSpec extends Specification {
    
  "Retweets of friends are identified correctly" in {
    assert("RT @dave Hi".isFromFriend(List("dave", "mary")))
    assert("Hi (via @dave)".isFromFriend(List("dave", "mary")))
    assert(! "Hi (via @dave2)".isFromFriend(List("dave", "mary")))
  }

  "Retweets are identified correctly" in {
    assert("RT @dave Hi".isRetweet)
    assert("Hi (via @dave2)".isRetweet)
    assert(! "The sky is blue".isRetweet)
  }
}
 