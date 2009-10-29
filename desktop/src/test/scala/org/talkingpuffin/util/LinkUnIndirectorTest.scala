package org.talkingpuffin.util

import org.junit.Test
import org.junit.Assert._
import org.apache.log4j.{Logger}

class LinkUnIndirectorTest {
  val log = Logger.getLogger("Tests")
    
  @Test def stumbleUpon = testExpand("http://su.pr/1XsUY4",
    "http://www.walletpop.com/blog/2009/10/27/23-items-worth-maxing-out-your-credit-card-before-world-ends/")
  
  @Test def digg = testExpand("http://digg.com/d318HBo?t",
    "http://www.treehugger.com/files/2009/10/tesla-electric-car-accident-denmark-prius-suv.php")
  
  @Test def friendFeed = testExpand("http://ff.im/auf8d",
    "http://fupeg.blogspot.com/2009/10/social-technology-fail.html")
  
  @Test def hootSuite = testExpand("http://ow.ly/xdVE",
    "http://www.meetup.com/TwitterMeetup/calendar/11708113/")
  
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
 