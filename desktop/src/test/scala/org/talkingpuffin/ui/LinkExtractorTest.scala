package org.talkingpuffin.ui

import org.junit.Test
import org.junit.Assert.assertEquals
import org.talkingpuffin.util.LinkExtractor

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

  @Test def allLinksAreFound {
    val links = LinkExtractor.getLinks("CNN lies...silent http://bit.ly/cnn-BS Real story here: http://bit.ly/dSUqXT",
      None, false, true, false)
    assertEquals(2, links.size)
  }

}