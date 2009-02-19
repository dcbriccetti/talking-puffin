package com.davebsoft.sctw.state

import java.io._

/**
 * Stores application state
 * @author Dave Briccetti
 */

object StateRepository {
  private val state = scala.collection.mutable.Map[String, String]()
  
  def set(key: String, value: String) {
    state += (key -> value)
  }
  
  def get(key: String) = state(key)
  
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
    val in = new BufferedReader(new FileReader(getFile))
    // I don’t like this 1970s “priming read” approach, but my 
    // while ((line = in.readLine()) != null) from Java didn’t work
    var line = in.readLine  
    while (line != null) {
      val kv = line.split("=")
      state += (kv(0) -> kv(1))
      line = in.readLine
    }
    in.close
  }
  
  def main(args: Array[String]) { // TODO move this to unit test
    StateRepository.set("k1", "v1")
    StateRepository.set("k2", "v2")
    StateRepository.save
    StateRepository.clear
    StateRepository.load
    println(StateRepository.get("k1"))
  }
}