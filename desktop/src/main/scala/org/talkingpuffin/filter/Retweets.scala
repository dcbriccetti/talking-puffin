package org.talkingpuffin.filter

import ui.LinkExtractor

object Retweets {
  private val rtUserRegex = ("""(rt|RT|♺)\:? ?""" + LinkExtractor.usernameRegex + ".*").r

  def fromFriend_?(text: String, friendUsernames: List[String]) = 
    try {
      val rtUserRegex(rtSymbol, username) = text
      friendUsernames.contains(username)
    } catch {
      case e: MatchError => false
    }
}

