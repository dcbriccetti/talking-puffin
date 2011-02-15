package org.talkingpuffin.state

import java.util.prefs.Preferences.userRoot
import swing.event.Event
import swing.Publisher

/**
 * Provides Preferences.
 */
object GlobalPrefs {
  val prefs = userRoot.node("/org/talkingpuffin/all")
  val publisher = new Publisher {}
  
  case class PrefChangedEvent(key: String, value: Any) extends Event
  
  def prefsForUser(service: String, username: String) = userRoot.node(serviceNodeName(service) + "/" + username)

  def prefsForService(service: String) = userRoot.node(serviceNodeName(service))

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
  
  def isOn(key: String) = prefs.getBoolean(key, false) 

  private def serviceNodeName(service: String) = "/org/talkingpuffin/streams/" + service.toLowerCase

}
