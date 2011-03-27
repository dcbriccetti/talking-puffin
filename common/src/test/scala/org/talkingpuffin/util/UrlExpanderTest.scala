package org.talkingpuffin.util

import org.specs.runner.JUnit4
import org.specs.Specification

class UrlExpanderTest extends JUnit4(UrlExpanderTest)

object UrlExpanderTest extends Specification {
  "URL expansion" should {
    "return the correct URL" in {
      UrlExpander.expand("http://bit.ly/g9zLHR") must be_==("http://english.stackexchange.com/questions/18122/valid-or-not-treat-others-with-the-same-respect-youd-want-them-to-treat-you/18191#18191")
    }
    "return the input when not existing" in {
      val noSuch = "http://bit.ly/nosuchlink143"
      UrlExpander.expand(noSuch) must be_==(noSuch)
    }
  }
}
