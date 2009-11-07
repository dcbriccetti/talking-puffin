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

  /**
   * Returns whether text contains a retweet.
   */
  def isRetweet = RetweetDetector.regexes.exists(_.findFirstIn(text).isDefined)
  
  /**
   * Returns whether text contains a “commented retweet,” which we define as a 
   * retweet with a comment before it. For example: <code>Great stuff! RT @joe We won</code>
   * is a retweet of @joe’s tweet with a comment before it.
   */
  def isCommentedRetweet = RetweetDetector.commentedRtUser.findFirstIn(text).isDefined

  /**
   *  Returns whether the text matches one of the known retweet patterns and the  
   * “retweet of” username matches one of the names provided.
   */
  def isRetweetOfStatusFromFriend(friendUsernames: List[String]) = 
    RetweetDetector.regexes.exists(regex => text match {
      case regex(_, username) => friendUsernames.contains(username)
      case _ => false
    })
}

