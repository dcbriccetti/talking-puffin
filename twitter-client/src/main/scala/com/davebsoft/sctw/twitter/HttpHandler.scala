package com.davebsoft.sctw.twitter

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.cookie.CookiePolicy
import org.apache.commons.httpclient.methods.{PostMethod, GetMethod}
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpMethodBase, HttpMethod, HttpClient}

/**
 * Generalises Http handling.
 * @author Alf Kristian Støyle  
 */
protected trait HttpHandler {

  val httpClient = new HttpClient()
  
  def doGet(url: String) = {
    val method = new GetMethod(url)
    handleCommonMethodSetup(method)
    val result = httpClient.executeMethod(method)
    val responseBody = method.getResponseBodyAsString()
    method.releaseConnection
    (method, result, responseBody)
  }
  
  def doPost(url: String) = {
    val method = new PostMethod(url)
    handleCommonMethodSetup(method)
    val result = httpClient.executeMethod(method)
    val responseBody = method.getResponseBodyAsString()
    method.releaseConnection
    (method, result, responseBody)
  }

  private def handleCommonMethodSetup(method: HttpMethod) {
    // Since every call is authenticated we do currently not use cookies.
    method.getParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES)
  }

  def setCredentials(username: String, password: String) {
    httpClient.getState().setCredentials(new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), 
      new UsernamePasswordCredentials(username, password))
    httpClient.getParams.setAuthenticationPreemptive(true)
  }
  
}
