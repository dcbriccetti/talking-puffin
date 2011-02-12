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

  private val tw = session.twitter
  val following     = new CommonTweetsProvider("Following", session, getHighest(0), progress, tw.getHomeTimeline)
  val mentions      = new CommonTweetsProvider("Mentions",  session, getHighest(1), progress, tw.getMentions,
    Some(StatusTableModelCust.Mentions))
  val retweetsOfMe  = new CommonTweetsProvider("RTs of Me", session, getHighest(2), progress, tw.getRetweetsOfMe)
  val retweetedByMe = new CommonTweetsProvider("RTs by Me", session, getHighest(3), progress, tw.getRetweetedByMe)
  val retweetedToMe = new CommonTweetsProvider("RTs to Me", session, getHighest(4), progress, tw.getRetweetedToMe)
  /*todo val dmsReceived   = new DmsReceivedProvider  (session, getHighest(5), progress)
  val dmsSent       = new DmsSentProvider      (session, getHighest(6), progress)*/

  val providers = List(following, mentions, retweetsOfMe, retweetedByMe,
    retweetedToMe)//todo, dmsReceived, dmsSent)
  val autoStartProviders = List(following, mentions)//todo, dmsReceived)
  val providersAndPrefKeys = providers zip prefKeys
  
  def stop = providers.foreach(_.stop)
}