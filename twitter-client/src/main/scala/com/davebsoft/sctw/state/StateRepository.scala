package com.davebsoft.sctw.state

import java.io._
import java.util.Properties

/**
 * Stores application state
 * @author Dave Briccetti
 */

object StateRepository {
  private val state = new Properties
  private var loaded = false
  
  def set(key: String, value: String) {
    state.setProperty(key, value)
  }
  
  def set(keyValues: Tuple2[String, String]*) {
    for ((key, value) <- keyValues) state.setProperty(key, value)
  }
  
  def remove(key: String*) {
    key.foreach(state remove _)
  }

  
  def get(key: String, default: String): String = {
    if (! loaded) 
      load
    state.getProperty(key, default)
  }
  
    def get(key: String): String = {
    if (! loaded) 
      load
    state.getProperty(key)
  }
  
  private def getFile = {
    val homeDir = new File(System.getProperty("user.home"));
    new File(homeDir, ".simple-twitter-client.properties");
  }
  
  def save = state.store(new PrintWriter(new FileWriter(getFile)), "Program state")
  
  def clear = state.clear
  
  def load {
    clear
    try {
      state.load(new FileReader(getFile))
    } catch {
      case ex: FileNotFoundException => // Ignore
    }
    loaded = true
  }
}