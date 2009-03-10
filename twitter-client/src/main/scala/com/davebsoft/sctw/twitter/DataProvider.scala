package com.davebsoft.sctw.twitter

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.methods.GetMethod

case class DataFetchException(val code: Int, val response: String) extends Exception
  
/**
 * A provider of Twitter data
 */
abstract class DataProvider extends HttpHandler {
  
  def getUrl: String

  /**
   * Load data for the appropriate Twitter service (determined by the subclass),
   * and return it as XML.
   */
  def loadTwitterData: Node = {
    loadTwitterData(getUrl)
  }
  
  def loadTwitterData(url: String): Node = {
    println(url)
    val (method, result, responseBody) = doGet(url)

    if (result != 200) {
      println(responseBody)
      throw new DataFetchException(result, responseBody)
    } else {
      XML.loadString(responseBody)
    }
  }
  
}

abstract class FriendsFollowersDataProvider(username: String, password: String) extends DataProvider {
  setCredentials(username, password)
  
  def getUsers: List[Node] = {
    val elem = loadTwitterData
    if (elem == null) {
      List[Node]()
    } else {
      val users = elem \ "user"
      var usersList = List[Node]()
      
      for (user <- users) {
        usersList ::= user
      }
    
      usersList.sort((a,b) => ((a \ "name").text.toLowerCase compareTo 
              (b \ "name").text.toLowerCase) < 0)
    }
  }
}

class FriendsDataProvider(username: String, password: String) 
    extends FriendsFollowersDataProvider(username, password) {

  def getUrl = "http://twitter.com/statuses/friends.xml"
}

class FollowersDataProvider(username: String, password: String) 
    extends FriendsFollowersDataProvider(username, password) {

  def getUrl = "http://twitter.com/statuses/followers.xml"
}

