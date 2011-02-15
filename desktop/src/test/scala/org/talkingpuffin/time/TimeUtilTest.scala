package org.talkingpuffin.time

import org.specs.runner.JUnit4
import org.specs.Specification
import org.joda.time.DateTime

import TimeUtil.{isToday, toAge}

class TimeUtilTest extends JUnit4(TimeUtilSpec)

object TimeUtilSpec extends Specification {
  private def now = new DateTime

 "One hour ago should be 3600s" in {
   toAge(now.minusHours(1)) must beCloseTo(3600L,2L)
 }
  "Future time should be negative seconds ago" in {
     toAge(now.plusHours(1)) must beCloseTo(-3600L,2L)
   }
  "One minute ago should be 60s" in {
     toAge(now.minusMinutes(1)) must beCloseTo(60L,2L)
   }
  "now should be 0s ago" in {
    toAge(now) must beCloseTo(0L,2L)
  }
  "now should be today"  in {
    isToday(now) must be(true)
  }
  "one year ago should not be today" in {
    isToday(now.minusYears(1)) must be(false)
  }
  "tomorow is not today" in {
    isToday(now.plusDays(1)) must be(false)
  }
  "now is now" in {
    now must beEqualTo(new DateTime)
  }
}