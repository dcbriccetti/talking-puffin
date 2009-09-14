package org.talkingpuffin.filter

import apache.log4j.Logger
import com.google.common.collect.{Multimap, HashMultimap}
import java.io.{File, PrintWriter, FileNotFoundException, FileWriter}
import java.util.ArrayList
import scala.io.Source
import state.PreferencesFactory

/**
 * Repository of tag -> user mappings, stored within a service/user
 */
class TagUsers(service: String, username: String) {
  private val log = Logger getLogger "TagUsers"
  private val prefs = PreferencesFactory.prefsForUser(service, username).node("tags")
  private val tagUsers: Multimap[String,Long] = HashMultimap.create()
  prefs.keys.foreach(tag => prefs.get(tag, null).split("\t").foreach(userId => 
      add(tag, java.lang.Long.parseLong(userId))))
  
  def add(tag: String, userId: Long) = tagUsers.put(tag, userId)
  
  def contains(tag: String, userId: Long) = tagUsers.get(tag).contains(userId)
  
  def removeForUser(userId: Long) {
    val it = tagUsers.entries.iterator
    while (it.hasNext) {
      val item = it.next
      if (item.getValue == userId) {
        log.debug("Removing " + item)
        it.remove
      }
    }
  }
  
  def getTags: List[String] = {
    var tags = List[String]()
    val keys = tagUsers.keySet.iterator
    while(keys.hasNext) {
      tags ::= keys.next
    }
    tags.sort(_ < _)
  }
  
  def tagsForUser(userId: Long): List[String] = {
    var tags = List[String]()
    val el = getEntriesAsList
    for (i <- 0 until el.size) {
      val tu = el.get(i)
      if (tu.getValue == userId) tags = tu.getKey :: tags
    }
    tags
  }
  
  def save {
    prefs.clear
    val el = getKeysAsList
    for (i <- 0 until el.size) {
      val tag = el.get(i)
      val users = new ArrayList[Long](tagUsers.get(tag))
      val sb = new StringBuilder
      for (j <- 0 until users.size) {
        sb.append(users.get(j)).append("\t")
      }
      prefs.put(tag, sb.toString)
    }
  }
  
  private def getEntriesAsList = new ArrayList[java.util.Map.Entry[String,Long]](tagUsers.entries) 
  private def getKeysAsList = new ArrayList[String](tagUsers.keys) 

}