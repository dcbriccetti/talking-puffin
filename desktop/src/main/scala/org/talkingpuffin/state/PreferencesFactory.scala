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
  
  def showColumn(col: String, showing: Boolean) {
    prefs.putBoolean(PrefKeys.SHOW_COL + col, showing)
  }
  
  def sortBy(col: String, direction: String) {
    prefs.put(PrefKeys.SORT_BY, col)
    prefs.put(PrefKeys.SORT_DIRECTION, direction)
  }
  
  def isColumnShowing(col: String): Boolean = prefs.getBoolean(PrefKeys.SHOW_COL + col, true)
}

object PrefKeys {
  val USE_ANIMATIONS    = "useAnimations"
  val LOOK_UP_LOCATIONS = "lookUpLocations"
  val EXPAND_URLS       = "expandUrls"
  val SORT_BY           = "sortBy"
  val SHOW_COL          = "showCol"
  val AGE               = "Age"
  val IMAGE             = "Image"
  val FROM              = "From"
  val TO                = "To"
  val SORT_DIRECTION    = "sortDirection"
  val SORT_DIRECTION_ASC   = "asc"
  val SORT_DIRECTION_DESC  = "desc"

  val gprefs = GlobalPrefs.prefs
  val keys = gprefs.keys
  // Set options that default to true
  for (k <- List(LOOK_UP_LOCATIONS, SHOW_COL + AGE, SHOW_COL + IMAGE, SHOW_COL + FROM, SHOW_COL + TO)) 
    if (! keys.contains(k))
      gprefs.putBoolean(k, true)
  
  // Set other defaults
  if (! keys.contains(SORT_BY))        gprefs.put(SORT_BY,        AGE)
  if (! keys.contains(SORT_DIRECTION)) gprefs.put(SORT_DIRECTION, SORT_DIRECTION_DESC)
}

