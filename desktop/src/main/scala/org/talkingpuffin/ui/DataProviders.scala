package org.talkingpuffin.ui

import java.util.prefs.Preferences
import org.talkingpuffin.state.PrefKeys._
import org.talkingpuffin.Session

class DataProviders(val session: Session, prefs: Preferences, progress: LongOpListener) {
  val prefKeys = List(HIGHEST_ID, HIGHEST_MENTION_ID, HIGHEST_RETWEET_OF_ME_ID, 
    HIGHEST_RETWEET_BY_ME_ID, HIGHEST_RETWEET_TO_ME_ID, 
    HIGHEST_RECEIVED_DM_ID, HIGHEST_SENT_DM_ID)

  private def getHighest(idx: Int): Option[Long] = 
    prefs.get(prefKeys(idx), null) match {case null => None; case v => Some(v.toLong)} 

  val following     = new FollowingProvider    (session, getHighest(0), progress)
  val mentions      = new MentionsProvider     (session, getHighest(1), progress)
  val retweetsOfMe  = new RetweetsOfMeProvider (session, getHighest(2), progress)
  val retweetedByMe = new RetweetedByMeProvider(session, getHighest(3), progress)
  val retweetedToMe = new RetweetedToMeProvider(session, getHighest(4), progress)
  val dmsReceived   = new DmsReceivedProvider  (session, getHighest(5), progress)
  val dmsSent       = new DmsSentProvider      (session, getHighest(6), progress)

  val providers = List(following, mentions, retweetsOfMe, retweetedByMe, 
    retweetedToMe, dmsReceived, dmsSent)
  val autoStartProviders = List(following, mentions, dmsReceived)
  val providersAndPrefKeys = providers zip prefKeys
  
  def stop = providers.foreach(_.stop)
}