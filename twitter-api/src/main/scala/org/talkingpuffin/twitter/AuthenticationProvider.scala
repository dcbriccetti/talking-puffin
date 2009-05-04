package org.talkingpuffin.twitter

import _root_.scala.xml.{XML, Node}
import java.io.IOException

class AuthenticationProvider extends HttpHandler {

  def userAuthenticates(userName: String, password: String): Option[Node] = {
    setCredentials(userName, password)
    try {
      val (statusCode, body) = doGet("http://twitter.com/account/verify_credentials.xml")
      if (statusCode == 200) return Some(XML.loadString(body))
      println(statusCode)
      None
    } catch {
      case e: IOException => {
        println(e)
        println(e.getCause)
        None
      }
    }
  }

}
