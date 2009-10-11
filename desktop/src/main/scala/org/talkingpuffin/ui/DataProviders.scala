package org.talkingpuffin.ui

import java.util.prefs.Preferences
import org.talkingpuffin.state.PrefKeys
import org.talkingpuffin.twitter.AuthenticatedSession

class DataProviders(user: AuthenticatedSession, prefs: Preferences, progress: LongOpListener) {
  val prefKeys = List(PrefKeys.HIGHEST_ID, PrefKeys.HIGHEST_MENTION_ID, PrefKeys.HIGHEST_RECEIVED_DM_ID, 
    PrefKeys.HIGHEST_SENT_DM_ID)

  private def getHighest(idx: Int) = 
    prefs.get(prefKeys(idx), null) match {case null => None; case v => Some(v.toLong)} 

  val followingProvider = new FollowingProvider(user, getHighest(0), progress)
  val mentionsProvider  = new MentionsProvider (user, getHighest(1), progress)
  val dmsReceivedProvider = new DmsReceivedProvider (user, getHighest(2), progress)
  val dmsSentProvider = new DmsSentProvider (user, getHighest(3), progress)

  val providers = List(followingProvider, mentionsProvider, dmsReceivedProvider, dmsSentProvider)
  val providersAndPrefKeys = providers zip prefKeys
}