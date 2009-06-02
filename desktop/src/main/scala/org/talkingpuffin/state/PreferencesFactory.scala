package org.talkingpuffin.state

import java.util.prefs.Preferences

/**
 * Provides Preferences.
 */

object PreferencesFactory {
  def prefsForUser(username: String) = 
    Preferences.userRoot.node("/org/talkingpuffin/streams/twitter/" + username)
  
}

object GlobalPrefs {
  val prefs = Preferences.userRoot.node("/org/talkingpuffin/all")
}

object PrefKeys {
  val USE_ANIMATIONS = "useAnimations"
  val LOOK_UP_LOCATIONS = "lookUpLocations"
  val EXPAND_URLS = "expandUrls"
  
  for (k <- List(LOOK_UP_LOCATIONS)) // Set options that default to true 
    if (! GlobalPrefs.prefs.keys.contains(k)) 
      GlobalPrefs.prefs.putBoolean(k, true)
}

