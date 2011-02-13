package org.talkingpuffin.ui

import java.util.prefs.Preferences
import org.talkingpuffin.state.PrefKeys._
import org.talkingpuffin.Session
import twitter4j.{Status, ResponseList, Paging}

class DataProviders(val session: Session, prefs: Preferences, progress: LongOpListener) {
  private val prefKeys = List(HIGHEST_ID, HIGHEST_MENTION_ID, HIGHEST_RETWEET_OF_ME_ID,
    HIGHEST_RETWEET_BY_ME_ID, HIGHEST_RETWEET_TO_ME_ID, 
    HIGHEST_RECEIVED_DM_ID, HIGHEST_SENT_DM_ID)

  private val tw = session.twitter
  val following     = create("Following", 0, tw.getHomeTimeline)
  val mentions      = create("Mentions",  1, tw.getMentions, Some(StatusTableModelCust.Mentions))
  val retweetsOfMe  = create("RTs of Me", 2, tw.getRetweetsOfMe)
  val retweetedByMe = create("RTs by Me", 3, tw.getRetweetedByMe)
  val retweetedToMe = create("RTs to Me", 4, tw.getRetweetedToMe)

  val providers = List(following, mentions, retweetsOfMe, retweetedByMe, retweetedToMe)
  val autoStartProviders = List(following, mentions)
  val providersAndPrefKeys = providers zip prefKeys
  
  def stop = providers.foreach(_.stop)

  private def getHighest(idx: Int): Option[Long] =
    prefs.get(prefKeys(idx), null) match {case null => None; case v => Some(v.toLong)}

  private def create(title: String, idx: Int, twFunc: (Paging) => ResponseList[Status],
    statusTableModelCust: Option[StatusTableModelCust.Value] = None) =
    new CommonTweetsProvider(title, session, getHighest(idx), progress, twFunc, statusTableModelCust)
}