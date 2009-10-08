package org.talkingpuffin.state

import net.lag.configgy.Configgy

object ConvertPrefs {
  def convert {

    val confName = "/Users/daveb/tpuf.conf"
    val f = new java.io.File(confName)
    if (! f.exists) f.createNewFile
    Configgy.configure(confName)
    val conf = Configgy.config
    val global = java.util.prefs.Preferences.userRoot.node("/org/talkingpuffin/all")
    global.keys.foreach(k => {
      val value = global.get(k, "")
      println("Writing " + k + " " + value)
      value match {
        case "true" => conf(k) = true
        case "false" => conf(k) = false
        case v => conf(k) = v
      }
    })
    println(conf)
  }
  
  def main(args: Array[String]): Unit = convert
}