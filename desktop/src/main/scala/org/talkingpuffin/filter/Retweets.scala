package org.talkingpuffin.filter

import org.talkingpuffin.ui.LinkExtractor

/**
 * Methods related to old-style (RT, etc.) retweets
 */
object Retweets {
  private val user = LinkExtractor.usernameRegex
  private val rtUser = ("""(rt|RT|♺)\:? ?""" + user + ".*").r
  private val viaUser = (""".*\((via|VIA|Via) +""" + user + """\)""").r
  private val regexes = List(rtUser, viaUser)

  def fromFriend_?(text: String, friendUsernames: List[String]) = regexes.exists(regex => text match {
    case regex(rtSymbol, username) => friendUsernames.contains(username)
    case _ => false
  })
}

