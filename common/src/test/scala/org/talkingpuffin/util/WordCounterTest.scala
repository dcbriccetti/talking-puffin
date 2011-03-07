package org.talkingpuffin.util

import org.specs.runner.JUnit4
import org.specs.Specification

class WordCounterTest extends JUnit4(WordCounterSpec)

object WordCounterSpec extends Specification {
  "The result" should {
    "contain 2 words" in {
      WordCounter("Scala traits").words must have size(2)
    }
    "have trailing punctuation stripped" in {
      WordCounter("Scala.").words(0).word must_==("scala")
    }
    "count correctly" in {
      WordCounter("Scala. scala SCala?").words(0).count must_==(3)
    }
  }
}
