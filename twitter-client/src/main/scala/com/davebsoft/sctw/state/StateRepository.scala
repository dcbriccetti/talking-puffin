package com.davebsoft.sctw.state

import java.io._
import scala.io._

/**
 * Stores application state
 * @author Dave Briccetti
 */

object StateRepository {
  private val state = scala.collection.mutable.Map[String, String]()
  private var loaded = false
  
  def set(key: String, value: String) {
    state += (key -> value)
  }
  
  def get(key: String, default: String): String = {
    if (! loaded) 
      load
    if (state.contains(key)) state(key) else default
  }
  
  private def getFile = {
    val homeDir = new File(System.getProperty("user.home"));
    new File(homeDir, ".simple-twitter-client.properties");
  }
  
  def save {
    val out = new PrintWriter(new FileWriter(getFile))
    for (key <- state.keys) {
      out.println(key + "=" + state(key))
    }
    out.close
  }
  
  def clear = state.clear
  
  def load {
    clear
    try {
      val src = Source.fromFile(getFile)  
      src.getLines.map(_.split("=")).foreach((kv) => state += kv(0) -> kv(1).trim)  
    } catch {
      case ex: FileNotFoundException => // Ignore
    }
    loaded = true
  }
}