package org.talkingpuffin.twitter

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import org.apache.log4j.Logger
import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.methods.GetMethod

case class DataFetchException(val code: Int, val response: String) extends Exception
  
/**
 * A provider of Twitter data
 */
abstract class DataProvider extends HttpHandler {
  private val log = Logger.getLogger("DataProvider")
  var urlHost = "http://twitter.com/" 
  
  def getUrl: String

  /**
   * Load data for the appropriate Twitter service (determined by the subclass),
   * and return it as XML.
   */
  def loadTwitterData: Node = loadTwitterData(getUrl)
  
  def loadTwitterData(url: String): Node = {
    val (result, responseBody) = doGet(url)

    if (result != 200) {
      log.error(responseBody)
      throw new DataFetchException(result, responseBody)
    }
    XML.loadString(responseBody)
  }
  
}

