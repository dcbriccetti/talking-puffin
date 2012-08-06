package org.talkingpuffin.filter

import com.google.common.collect.{Multimap, HashMultimap}
import org.talkingpuffin.state.GlobalPrefs.prefsForUser
import collection.mutable

/**
 * Repository of tag -> user mappings, stored within a service/user
 */
class TagUsers(service: String, username: String) {
  private val userPrefs = prefsForUser(service, username)
  private val tagsPrefs = userPrefs.node("tags")
  private val tagDescPrefs = userPrefs.node("tagDescs")
  private val tagUsers: Multimap[String,Long] = HashMultimap.create()
  private val tagDescs = mutable.Map[String,String]()
  
  load()
  
  def add(tag: String, userId: Long) = tagUsers.put(tag, userId)
  
  def addDescription(tag: String, desc: String) {
    tagDescs(tag) = desc
  }
  
  def getDescription(tag: String): Option[String] = tagDescs.get(tag)
  
  def contains(tag: String, userId: Long) = tagUsers.get(tag).contains(userId)
  
  def removeForUser(userId: Long) {
    val it = tagUsers.entries.iterator
    while (it.hasNext) {
      val item = it.next
      if (item.getValue == userId) {
        it.remove()
      }
    }
  }
  
  def getTags: List[String] = itToList(tagUsers.keySet).sort(_ < _)
  
  def getTagsWithCounts: List[Tuple2[String,Int]] = getTags.map(tag => (tag, usersForTag(tag).length))
  
  def tagsForUser(userId: Long): List[String] = itToList(tagUsers.entries).filter(_.getValue == userId).map(_.getKey)

  def usersForTag(tag: String): List[Long] = itToList(tagUsers.get(tag))
  
  def save() {
    tagsPrefs.clear()
    tagDescPrefs.clear()
    for {
      tag <- itToList(tagUsers.keys)
      str = itToList(tagUsers.get(tag)).mkString("\t")
    } {
      tagsPrefs.put(tag, str)
      tagDescs.get(tag).foreach(tagDescPrefs.put(tag, _))
    }
  }
  
  private def load() {
    tagsPrefs.keys.foreach(tag => tagsPrefs.get(tag, null).split("\t").foreach(userId => add(tag, userId.toLong)))
    tagDescPrefs.keys.foreach(tag => tagDescs(tag) = tagDescPrefs.get(tag, null))
  }
  
  private def itToList[T](it: java.lang.Iterable[T]): List[T] = itToList(it.iterator)
  
  private def itToList[T](it: java.util.Iterator[T]): List[T] = {
    var l = List[T]()
    while (it.hasNext) {
      l ::= it.next
    }
    l.reverse
  }
}
