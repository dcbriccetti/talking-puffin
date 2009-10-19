package org.talkingpuffin.filter

import org.specs.runner.JUnit4
import org.specs.Specification
import org.talkingpuffin.ui.LinkExtractor

class LinkExtractorTest extends JUnit4(LinkExtractorSpec)
 
object LinkExtractorSpec extends Specification {
    
  "User lists and links are found" in {
    LinkExtractor.createLinks("ABC @dave/scala 123") must_== 
        "ABC <a href='http://twitter.com/dave/scala'>@dave/scala</a> 123"
  }

}
 