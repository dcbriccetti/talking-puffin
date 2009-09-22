package org.talkingpuffin.state

import java.util.prefs.Preferences
import swing.event.Event
import swing.Publisher

/**
 * Provides Preferences.
 */
object PreferencesFactory {
  def prefsForUser(service: String, username: String) =
    Preferences.userRoot.node("/org/talkingpuffin/streams/" + service.toLowerCase + "/" + username)
}

object GlobalPrefs {
  val prefs = Preferences.userRoot.node("/org/talkingpuffin/all")
  val publisher = new Publisher {}
  
  case class PrefChangedEvent(val key: String, val value: Any) extends Event
  
  def put(key: String, value: Boolean) = {
    prefs.putBoolean(key, value)
    publisher.publish(new PrefChangedEvent(key, value))
  }
  
  def showColumn(col: String, showing: Boolean) {
    prefs.putBoolean(PrefKeys.SHOW_COL_PREFIX + col, showing)
  }
  
  def sortBy(col: String, direction: String) {
    prefs.put(PrefKeys.SORT_BY, col)
    prefs.put(PrefKeys.SORT_DIRECTION, direction)
  }
  
  def isColumnShowing(col: String): Boolean = prefs.getBoolean(PrefKeys.SHOW_COL_PREFIX + col, true)
}

object PrefKeys {
  val USE_ANIMATIONS    = "useAnimations"
  val USE_REAL_NAMES    = "useRealNames"
  val NOTIFY_TWEETS     = "notifyTweets"
  val LOOK_UP_LOCATIONS = "lookUpLocations"
  val EXPAND_URLS       = "expandUrls"
  val SHOW_TWEET_DATE_AS_AGE = "showTweetDateAsAge"
  val SORT_BY           = "sortBy"
  
  val HIGHEST_ID        = "highestId"
  val HIGHEST_MENTION_ID= "highestMentionId"
  val HIGHEST_RECEIVED_DM_ID = "highestReceivedDmId"
  val HIGHEST_SENT_DM_ID     = "highestSentDmId"
  
  val SHOW_COL_PREFIX   = "showCol"
  val AGE               = "Age"
  val IMAGE             = "Image"
  val FROM              = "From"
  val TO                = "To"
  
  val SORT_DIRECTION    = "sortDirection"
  val SORT_DIRECTION_ASC   = "asc"
  val SORT_DIRECTION_DESC  = "desc"
  val STATUS_TABLE_STATUS_FONT_SIZE = "statusTableStatusFontSize"
  val STATUS_TABLE_ROW_HEIGHT = "statusTableRowHeight"

  val gprefs = GlobalPrefs.prefs
  val keys = gprefs.keys
  // Set options that default to true
  for (k <- List(USE_ANIMATIONS, USE_REAL_NAMES, LOOK_UP_LOCATIONS, NOTIFY_TWEETS, SHOW_TWEET_DATE_AS_AGE) ::: 
      List(AGE, IMAGE, FROM, TO).map(SHOW_COL_PREFIX + _)) 
    if (! keys.contains(k))
      gprefs.putBoolean(k, true)
  
  // Set other defaults
  if (! keys.contains(SORT_BY))        gprefs.put(SORT_BY,        AGE)
  if (! keys.contains(SORT_DIRECTION)) gprefs.put(SORT_DIRECTION, SORT_DIRECTION_DESC)
}

