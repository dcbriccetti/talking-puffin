package org.talkingpuffin.filter

import scala.collection.mutable.Map
import com.google.common.collect.{Multimap, HashMultimap}
import org.talkingpuffin.state.PreferencesFactory

/**
 * Repository of tag -> user mappings, stored within a service/user
 */
class TagUsers(service: String, username: String) {
  private val tagsPrefs = PreferencesFactory.prefsForUser(service, username).node("tags")
  private val tagDescPrefs = PreferencesFactory.prefsForUser(service, username).node("tagDescs")
  private val tagUsers: Multimap[String,Long] = HashMultimap.create()
  private val tagDescs = Map[String,String]()
  tagsPrefs.keys.foreach(tag => tagsPrefs.get(tag, null).split("\t").foreach(userId => add(tag, userId.toLong)))
  tagDescPrefs.keys.foreach(tag => tagDescs(tag) = tagDescPrefs.get(tag, null))
  
  def add(tag: String, userId: Long) = tagUsers.put(tag, userId)
  
  def addDescription(tag: String, desc: String) = tagDescs(tag) = desc
  
  def getDescription(tag: String): Option[String] = tagDescs.get(tag)
  
  def contains(tag: String, userId: Long) = tagUsers.get(tag).contains(userId)
  
  def removeForUser(userId: Long) {
    val it = tagUsers.entries.iterator
    while (it.hasNext) {
      val item = it.next
      if (item.getValue == userId) {
        it.remove
      }
    }
  }
  
  def getTags: List[String] = itToList(tagUsers.keySet.iterator).sort(_ < _)
  
  def getTagsWithCounts: List[Tuple2[String,Int]] = {
    getTags map(tag => (tag, usersForTag(tag).length))
  }
  
  def tagsForUser(userId: Long): List[String] = {
    for (tagUser <- itToList(tagUsers.entries.iterator)
      if (tagUser.getValue == userId)
    ) yield tagUser.getKey
  }
  
  def usersForTag(tag: String): List[Long] = itToList(tagUsers.get(tag).iterator)
  
  def save: Unit = {
    tagsPrefs.clear
    tagDescPrefs.clear
    for {
      tag <- itToList(tagUsers.keys.iterator)
      str = itToList(tagUsers.get(tag).iterator).mkString("\t")
    } {
      tagsPrefs.put(tag, str)
      tagDescs.get(tag) match {
        case Some(desc) => tagDescPrefs.put(tag, desc)
        case _ =>
      }
    }
  }
  
  private def itToList[T](it: java.util.Iterator[T]): List[T] = {
    var l = List[T]()
    while (it.hasNext) {
      l ::= it.next
    }
    l.reverse
  } 
  
}