package org.talkingpuffin.ui

import java.util.prefs.Preferences
import org.talkingpuffin.state.PrefKeys._
import org.talkingpuffin.twitter.AuthenticatedSession

class DataProviders(val twitterSession: AuthenticatedSession, prefs: Preferences, progress: LongOpListener) {
  val prefKeys = List(HIGHEST_ID, HIGHEST_MENTION_ID, HIGHEST_RECEIVED_DM_ID, HIGHEST_SENT_DM_ID)

  private def getHighest(idx: Int): Option[Long] = 
    prefs.get(prefKeys(idx), null) match {case null => None; case v => Some(v.toLong)} 

  val followingProvider = new FollowingProvider(twitterSession, getHighest(0), progress)
  val mentionsProvider  = new MentionsProvider (twitterSession, getHighest(1), progress)
  val dmsReceivedProvider = new DmsReceivedProvider (twitterSession, getHighest(2), progress)
  val dmsSentProvider = new DmsSentProvider (twitterSession, getHighest(3), progress)

  val providers = List(followingProvider, mentionsProvider, dmsReceivedProvider, dmsSentProvider)
  val providersAndPrefKeys = providers zip prefKeys
  
  def stop = providers.foreach(_.stop)
}