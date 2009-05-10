package org.talkingpuffin.state

import java.util.prefs.Preferences

/**
 * Provides Preferences.
 * 
 * @author Dave Briccetti
 */

object PreferencesFactory {
  def prefsForUser(username: String) = 
    Preferences.userRoot.node("/org/talkingpuffin/streams/twitter/" + username)
  
  def prefs = Preferences.userRoot.node("/org/talkingpuffin/all")
  
}