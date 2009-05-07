package org.talkingpuffin.twitter

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import org.apache.log4j.Logger
import scala.xml._
import javax.swing.table.AbstractTableModel
import org.apache.commons.httpclient.methods.GetMethod

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
  def loadTwitterData:HttpResponse = loadTwitterData(getUrl)
  
  def loadTwitterData(url: String) = {
    doGet(url) match {
      case HttpSuccess(code,responseBody) => HttpXMLSuccess(code,responseBody,XML.loadString(responseBody))
      case r:HttpResponse => r
    }
  }
  
}

