package com.davebsoft.sctw.twitter

class AuthenticationProvider extends HttpHandler {

  def userAuthenticates(userName: String, password: String): Boolean = {
    setCredentials(userName, password)
    val (method, result, body) = doGet("http://twitter.com/account/verify_credentials.json")
    if (result == 200) return true
    println(result)
    false
  }

}
