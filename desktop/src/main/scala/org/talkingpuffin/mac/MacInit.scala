package org.talkingpuffin.mac

/**
 * Initialize for Mac environment
 */
object MacInit {
  /**
   * Performs initializations for Mac including setting the menu bar and “about” name.
   */
  def init(title: String) {
    val props = System.getProperties
    props setProperty("apple.laf.useScreenMenuBar", "true")
    props setProperty("com.apple.mrj.application.apple.menu.about.name", title)
  }
}