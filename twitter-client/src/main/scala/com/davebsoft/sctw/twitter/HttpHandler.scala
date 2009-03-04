package com.davebsoft.sctw.twitter

import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.auth.AuthScope

/**
 * Generalises Http handling.
 * @author Alf Kristian Støyle  
 */
protected trait HttpHandler {

  val httpClient = new HttpClient()
  
  def doGet(url: String) = {
    val method = new GetMethod(url)
    val result = httpClient.executeMethod(method)
    val responseBody = method.getResponseBodyAsString()
    method.releaseConnection
    (method, result, responseBody)
  }

  def setCredentials(username: String, password: String) {
    httpClient.getState().setCredentials(new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), 
      new UsernamePasswordCredentials(username, password));
  }
  
}
