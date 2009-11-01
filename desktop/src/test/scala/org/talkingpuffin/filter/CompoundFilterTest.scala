package org.talkingpuffin.filter

import org.junit.Test
import org.junit.Assert._
import org.talkingpuffin.twitter.{TwitterUser, TwitterStatus}

class CompoundFilterTest {
  @Test def matchesText {
    val cf = CompoundFilter(None, Some(TextTextFilter("abc", false)), None, None, None)
    val status = new TwitterStatus
    status.text = "Hi there aBc def"
    assertTrue(cf.matches(status))
  }

  @Test def doesNotMatchText {
    val cf = CompoundFilter(None, Some(TextTextFilter("abc", false)), None, None, None)
    val status = new TwitterStatus
    status.text = "Hi there"
    assertFalse(cf.matches(status))
  }

  @Test def matchesCompound {
    val cf = CompoundFilter(None, Some(TextTextFilter("abc", false)), None, 
      Some(SourceTextFilter("web", false)), None)
    val status = new TwitterStatus
    status.text = "Hi there aBc def"
    status.sourceName = "web"
    assertTrue(cf.matches(status))
  }

  @Test def matchesMultipleCompound {
    val cfs = new CompoundFilters
    val cf1 = CompoundFilter(None, Some(TextTextFilter("abc", false)), None, 
      Some(SourceTextFilter("web", false)), None)
    val cf2 = CompoundFilter(Some(FromTextFilter("bill", false)), Some(TextTextFilter("def", false)), None, 
      None, None)
    cfs.list :::= List(cf1, cf2)
    val status = new TwitterStatus
    status.text = "Hi there aBc def"
    status.sourceName = "web"
    status.user = new TwitterUser {
      screenName = "bill"
    }
    assertTrue(cfs.matchesAll(status))
  }
}