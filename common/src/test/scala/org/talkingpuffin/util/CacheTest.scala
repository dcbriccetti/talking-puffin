package org.talkingpuffin.util

import org.specs.runner.JUnit4
import org.specs.Specification

class CacheTest extends JUnit4(CacheTest)

object CacheTest extends Specification {
  "Caching" should {
    "return what was stored" in {
      val cache = Cache[String]("test1")
      cache.put("a", "ax")
      cache.get("a") must be_==(Some("ax"))
    }
    "return none when not existing" in {
      val cache = Cache[String]("test1")
      cache.get("[no such key]") must be_==(None)
    }
  }
}
