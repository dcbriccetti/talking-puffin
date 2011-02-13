package org.talkingpuffin.filter

import org.junit.Test
import org.junit.Assert.assertEquals
import org.talkingpuffin.ui.LinkExtractor

class LinkExtractorTest {

  @Test def userListsAndLinksAreFound {
    assertEquals("ABC <a href='http://twitter.com/dave/scala'>@dave/scala</a> 123",
        LinkExtractor.createLinks("ABC @dave/scala 123"))
  }

  @Test def punctuationAtEndOfLinkIsNotIncludedInTheLink {
    assertEquals("ABC <a href='http://twitter.com/dave/scala'>@dave/scala</a>. 123",
      LinkExtractor.createLinks("ABC @dave/scala. 123"))

    assertEquals("ABC <a href='http://davebsoft.com'>http://davebsoft.com</a>. 123",
      LinkExtractor.createLinks("ABC http://davebsoft.com. 123"))

    assertEquals("ABC <a href='http://davebsoft.com'>http://davebsoft.com</a>, 123",
      LinkExtractor.createLinks("ABC http://davebsoft.com, 123"))
  }

}
 