package com.davebsoft.sctw.twitter

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.methods.GetMethod

/**
 * A provider of Twitter data
 */
abstract class DataProvider {
  private val httpClient = new HttpClient()
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
    val method = new GetMethod(url)
    val result = httpClient.executeMethod(method)
    if (result != 200) {
      println("Result: " + result)
      println(method.getResponseBodyAsString())
      null
    } else {
      val elem = XML.load(method.getResponseBodyAsStream())
      method.releaseConnection
      elem
    }
    
  }

  protected def setCredentials(username: String, password: String) {
    httpClient.getState().setCredentials(new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), 
      new UsernamePasswordCredentials(username, password));
  }
}

abstract class StatusDataProvider extends DataProvider {
  protected var highestId: String = null
  
  protected def setHighestId(highestId: String) {
    this.highestId = highestId
  }
  
  /**
   * Fetches statuses from Twitter.
   */
  def loadTwitterStatusData: NodeSeq = {
    formatData(loadTwitterData)
  }
  
  /**
   * Fetches statuses from Twitter.
   */
  def loadLastSet: NodeSeq = {
    formatData(loadTwitterData("http://twitter.com/statuses/friends_timeline.xml?count=200"))
  }
  
  def formatData(elem: Node): NodeSeq = {
    if (elem != null) {
      val statuses = elem \\ "status"
      if (statuses.length > 0) {
        highestId = (statuses(0) \ "id").text 
      }
      return statuses
    }
    List[Node]()
  }
}

class TweetsProvider(username: String, password: String, startingId: String) extends StatusDataProvider {
  setCredentials(username, password)
  if (startingId != null) 
    setHighestId(startingId)
  
  def getUrl = "http://twitter.com/statuses/friends_timeline.xml?count=200" +
      (if (highestId == null) "" else "&since_id=" + highestId)
  
  def getHighestId = highestId
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
  
  def getUserNames: List[String] = {
    // TODO call getUsers instead of duplicating this code
    val elem = loadTwitterData
    if (elem == null) {
      List[String]()
    } else {
      val users = elem \ "user"
      var userNames = List[String]()
      
      for (user <- users) {
        val userName = user \ "name"
        userNames ::= userName.text
      }
    
      userNames.sort((a,b) => (a.toLowerCase compareTo b.toLowerCase) < 0)
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

