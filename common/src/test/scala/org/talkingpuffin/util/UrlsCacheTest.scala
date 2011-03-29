package org.talkingpuffin.util

import org.specs.runner.JUnit4
import org.specs.Specification

class UrlsCacheTest extends JUnit4(UrlsCacheTest)

object UrlsCacheTest extends Specification {
  "Cacheing" should {
    "return what was stored" in {
      val cache = UrlsCache()
      cache.put("a", "ax")
      cache.get("a") must be_==(Some("ax"))
    }
    "return none when not existing" in {
      val cache = UrlsCache()
      cache.get("[no such key]") must be_==(None)
    }
  }
}
