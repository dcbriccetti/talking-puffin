package org.talkingpuffin.state

/**
 * Keys used for storing and retrieving preferences.
 */
object PrefKeys {
  val ACCESS_TOKEN      = "accessToken"
  val ACCESS_TOKEN_SECRET = "accessTokenSecret"
  val USE_ANIMATIONS    = "useAnimations"
  val USE_REAL_NAMES    = "useRealNames"
  val NOTIFY_TWEETS     = "notifyTweets"
  val LOOK_UP_LOCATIONS = "lookUpLocations"
  val EXPAND_URLS       = "expandUrls"
  val SHOW_TWEET_DATE_AS_AGE = "showTweetDateAsAge"
  val NEW_AFTER_CLEAR   = "newAfterClear"
  
  val SORT_BY           = "sortBy"
  
  val HIGHEST_ID        = "highestId"
  val HIGHEST_MENTION_ID= "highestMentionId"
  val HIGHEST_RETWEET_OF_ME_ID= "highestRetweetOfMeId"
  val HIGHEST_RETWEET_BY_ME_ID= "highestRetweetByMeId"
  val HIGHEST_RETWEET_TO_ME_ID= "highestRetweetToMeId"
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
