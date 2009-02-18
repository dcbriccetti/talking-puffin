package com.davebsoft.sctw

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.methods.GetMethod

abstract class DataProvider {
  val httpClient = new HttpClient()
  def getUrl: String
  val method = new GetMethod(getUrl)

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

class PublicStatusDataProvider() extends StatusDataProvider {
  def getUrl() = "http://twitter.com/statuses/public_timeline.xml"
}

class FriendsStatusDataProvider(username: String, password: String) extends StatusDataProvider {
  httpClient.getState().setCredentials(new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), 
    new UsernamePasswordCredentials(username, password));
  
  def getUrl() = "http://twitter.com/statuses/friends_timeline.xml"
}
