package org.talkingpuffin.filter

import ui.LinkExtractor

object Retweets {
  private val user = LinkExtractor.usernameRegex
  private val rtUser = ("""(rt|RT|♺)\:? ?""" + user + ".*").r
  private val viaUser = (""".*\((via|VIA|Via) +""" + user + """\)""").r

  def fromFriend_?(text: String, friendUsernames: List[String]): Boolean = {
    List(rtUser, viaUser).foreach(regex =>
      try {
        val regex(rtSymbol, username) = text
        if (friendUsernames.contains(username)) return true
      } catch {
        case e: MatchError => 
      })
    false
  }
}

