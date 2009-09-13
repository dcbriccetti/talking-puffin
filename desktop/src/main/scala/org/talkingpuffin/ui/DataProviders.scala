package org.talkingpuffin.ui

import java.util.prefs.Preferences
import state.{PrefKeys}
import twitter.AuthenticatedSession

class DataProviders(user: AuthenticatedSession, prefs: Preferences, progress: LongOpListener) {
  private def getHighest(key: String) = 
    prefs.get(key, null) match {case null => None; case v => Some(java.lang.Long.parseLong(v))} 
  val followingProvider = new FollowingProvider(user, getHighest(PrefKeys.HIGHEST_ID), progress)
  val mentionsProvider  = new MentionsProvider (user, getHighest(PrefKeys.HIGHEST_MENTION_ID), progress)
  val dmsReceivedProvider = new DmsReceivedProvider (user, getHighest(PrefKeys.HIGHEST_RECEIVED_DM_ID), progress)
  val dmsSentProvider = new DmsSentProvider (user, getHighest(PrefKeys.HIGHEST_SENT_DM_ID), progress)
  val providers = List(followingProvider, mentionsProvider, dmsReceivedProvider, dmsSentProvider)  
}