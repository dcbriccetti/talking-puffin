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
  val httpClient = new HttpClient()
  def getUrl: String
  val method = new GetMethod(getUrl)

  /**
   * Load data for the appropriate Twitter service (determined by the subclass),
   * and return it as XML.
   */
  def loadTwitterData: Node = {
    val result = httpClient.executeMethod(method)
    if (result != 200) {
      println("Result: " + result)
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
  /**
   * Fetches statuses from Twitter and stores them in <code>statuses</code>.
   */
  def loadTwitterStatusData(statuses: java.util.List[Node]) {
    val elem = loadTwitterData
    if (elem != null) {
      statuses.clear
      for (st <- elem \\ "status") {
        statuses.add(st)
      }
    }
  }
}

class PublicStatusDataProvider extends StatusDataProvider {
  def getUrl = "http://twitter.com/statuses/public_timeline.xml"
}

class FriendsStatusDataProvider(username: String, password: String) extends StatusDataProvider {
  setCredentials(username, password)
  
  def getUrl() = "http://twitter.com/statuses/friends_timeline.xml"
}

abstract class FriendsFollowersDataProvider(username: String, password: String) extends DataProvider {
  setCredentials(username, password)
  
  def getUserNames: List[String] = {
    val elem = loadTwitterData
    val users = elem \ "user"
    var userNames = List[String]()
    
    for (user <- users) {
      val userName = user \ "name"
      userNames ::= userName.text
    }
  
    userNames.sort((a,b) => (a.toLowerCase compareTo b.toLowerCase) < 0)
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

