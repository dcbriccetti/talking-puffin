package com.davebsoft.sctw.twitter

class AuthenticationProvider extends HttpHandler {

  def userAuthenticates(userName: String, password: String) = {
    setCredentials(userName, password)
    val (method, result, body) = doGet("http://twitter.com/account/verify_credentials.json")
    println(result)
    result == 200
  }

}
