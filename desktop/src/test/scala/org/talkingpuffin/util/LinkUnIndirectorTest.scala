package org.talkingpuffin.util

import org.junit.Test
import org.junit.Assert._
import org.apache.log4j.{Logger}

class LinkUnIndirectorTest {
  val log = Logger.getLogger("Tests")
    
//  @Test def stumbleUpon = testExpand("http://su.pr/1XsUY4",
//    "http://www.stumbleupon.com/su/1XsUY4/www.walletpop.com/blog/2009/10/27/23-items-worth-maxing-out-your-credit-card-before-world-ends/")

//  @Test def digg = testExpand("http://digg.com/story/r/Scala_and_GWT",
//    "http://www.eishay.com/2009/07/scala-on-google-app-engine-playing-it.html")
  
  @Test def bitly = testExpand("http://bit.ly/vO47k",
    "http://www.pushing-pixels.org/?p=1499")
  
  @Test def friendFeed = testExpand("http://ff.im/auf8d",
    "http://fupeg.blogspot.com/2009/10/social-technology-fail.html")
  
  @Test def hootSuite = testExpand("http://ow.ly/xdVE",
    "http://www.meetup.com/TwitterMeetup/calendar/11708113/")

   @Test def dummyTest =  assertTrue(true) 
  
  private def testExpand(startUrl: String, targetUrl: String) {
    var expanded = ""
    LinkUnIndirector.findLinks(
      (expandedUrl: String) => {
        expanded = expandedUrl
      }, 
      (unexpandedUrl: String) => {
        fail("In unexpanded callback: " + unexpandedUrl)
      })(startUrl)
    Thread.sleep(2000)
    assertEquals(targetUrl, expanded)
  }

}
