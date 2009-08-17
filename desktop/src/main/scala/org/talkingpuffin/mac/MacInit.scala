package org.talkingpuffin.mac

/**
 * Initialize for Mac environment
 */
object MacInit {
  def init(title: String) {
    val props = System.getProperties
    props setProperty("apple.laf.useScreenMenuBar", "true")
    props setProperty("com.apple.mrj.application.apple.menu.about.name", title)
  }
}