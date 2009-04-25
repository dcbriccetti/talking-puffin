package com.davebsoft.sctw.twitter

import java.io.IOException

class AuthenticationProvider extends HttpHandler {

  def userAuthenticates(userName: String, password: String): Boolean = {
    setCredentials(userName, password)
    try {
      val (result, body) = doGet("http://twitter.com/account/verify_credentials.json")
      if (result == 200) return true
      println(result)
      false
    } catch {
      case e: IOException => {
        println(e)
        println(e.getCause)
        false
      }
    }
  }

}
