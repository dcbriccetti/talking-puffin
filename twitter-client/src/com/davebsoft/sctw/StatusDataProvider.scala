package com.davebsoft.sctw

import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod

class StatusDataProvider {
  val httpClient = new HttpClient()
  val timelineMethod = new GetMethod("http://twitter.com/statuses/public_timeline.xml")
  
  /**
   * Fetches statuses from Twitter and stores them in statuses field.
   */
  def loadTwitterData(statuses: java.util.List[Node]) {
    val result = httpClient.executeMethod(timelineMethod)
    val timeline = XML.load(timelineMethod.getResponseBodyAsStream())
    statuses.clear
    for (st <- timeline \\ "status") {
      statuses.add(st)
    }
  }
  
}