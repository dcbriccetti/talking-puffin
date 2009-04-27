package com.davebsoft.sctw.filter

import java.io.{File, PrintWriter, FileNotFoundException, FileWriter}
import scala.collection.mutable.Set
import scala.io.Source
import state.StateRepository

/**
 * Repository of tag -> user mappings
 * @author Dave Briccetti
 */

case class TagUser(tag: String, userId: String)

/**
 * A set of tag -> user pairings
 */
object TagUsers {
  private val tagUsers = Set[TagUser]()
  
  def add(tagUser: TagUser) {
    tagUsers += tagUser
  }
  
  def contains(tagUser: TagUser): Boolean = {
    tagUsers.contains(tagUser)
  }
  
  def tagsForUser(userId: String): List[String] = {
    for (tu <- tagUsers.toList; if (tu.userId.equals(userId))) yield tu.tag
  }

  private def getFile = {
    val homeDir = new File(System.getProperty("user.home"));
    new File(homeDir, ".simple-twitter-client-tag-assigns.properties");
  }
                                  
  def load {
    tagUsers.clear
    try {
      val src = Source.fromFile(getFile)  
      src.getLines.map(_.split("=")).foreach((tu) => tagUsers += new TagUser(tu(0), tu(1).trim))  
    } catch {
      case ex: FileNotFoundException => // Ignore
    }
  }
  
  def save {
    val out = new PrintWriter(new FileWriter(getFile))
    for (tu <- tagUsers) {
      out.println(tu.tag + "=" + tu.userId)
    }
    out.close
  }
}