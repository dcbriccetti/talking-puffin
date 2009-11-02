package org.talkingpuffin.filter

import org.talkingpuffin.ui.LinkExtractor
import org.talkingpuffin.twitter.TwitterStatus

/**
 * Methods related to old-style (RT, via, etc.) retweets
 */
object RetweetDetector {
  private val user = LinkExtractor.usernameRegex
  private val rtUserString = """(rt|RT|♺)\:? ?""" + user + ".*"
  private val rtUser = rtUserString.r
  private val commentedRtUser = (".*? " + rtUserString).r
  private val viaUser = (""".*\((via|VIA|Via) +""" + user + """\)""").r
  private val regexes = List(rtUser, viaUser)
  
  implicit def string2RetweetDetector(text: String) = new RetweetDetector(text)
  implicit def status2RetweetDetector(status: TwitterStatus) = new RetweetDetector(status.text)
}
  
class RetweetDetector(text: String) {
  def isRetweet = RetweetDetector.regexes.exists(regex => text match {
    case regex(_, _) => true
    case _ => false
  })
  
  def isCommentedRetweet = text match {
    case RetweetDetector.commentedRtUser(_, _) => true
    case _ => false
  }
  
  def isRetweetOfStatusFromFriend(friendUsernames: List[String]) = 
    RetweetDetector.regexes.exists(regex => text match {
      case regex(_, username) => friendUsernames.contains(username)
      case _ => false
    })
}

