package com.davebsoft.sctw

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.methods.GetMethod

abstract class StatusDataProvider() {
  val httpClient = new HttpClient()
  def getUrl: String
  val method = new GetMethod(getUrl)
  
  /**
   * Fetches statuses from Twitter and stores them in <code>statuses</code>.
   */
  def loadTwitterData(statuses: java.util.List[Node]) {
    val result = httpClient.executeMethod(method)
    val timeline = XML.load(method.getResponseBodyAsStream())
    method.releaseConnection
    statuses.clear
    for (st <- timeline \\ "status") {
      statuses.add(st)
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
