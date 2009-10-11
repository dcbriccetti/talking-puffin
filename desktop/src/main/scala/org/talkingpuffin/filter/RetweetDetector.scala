package org.talkingpuffin.filter

import org.talkingpuffin.ui.LinkExtractor
import org.talkingpuffin.twitter.TwitterStatus

/**
 * Methods related to old-style (RT, via, etc.) retweets
 */
object RetweetDetector {
  private val user = LinkExtractor.usernameRegex
  private val rtUser = ("""(rt|RT|♺)\:? ?""" + user + ".*").r
  private val viaUser = (""".*\((via|VIA|Via) +""" + user + """\)""").r
  private val regexes = List(rtUser, viaUser)
  
  implicit def string2Retweet(text: String) = new RetweetDetector(text)
  implicit def status2Retweet(status: TwitterStatus) = new RetweetDetector(status.text)
}
  
class RetweetDetector(text: String) {
  def isRetweet = RetweetDetector.regexes.exists(regex => text match {
    case regex(rtSymbol, username) => true
    case _ => false
  })
  
  def isFromFriend(friendUsernames: List[String]) = RetweetDetector.regexes.exists(regex => text match {
    case regex(rtSymbol, username) => friendUsernames.contains(username)
    case _ => false
  })
}

