package org.talkingpuffin.twitter

import _root_.scala.xml.{XML, Node}
import java.io.IOException

class AuthenticationProvider extends HttpHandler {

  def userAuthenticates(userName: String, password: String): Option[Node] = {
    userAuthenticates(userName,password,API.defaultURL)
  }

  def userAuthenticates(userName: String, password: String, apiURL: String): Option[Node] = {
    setCredentials(userName, password)
    try {
      doGet(apiURL + "/account/verify_credentials.xml") match {
        case HttpSuccess(_,response) => Some(XML.loadString(response))
        case HttpException(e) => {
            println(e)
            println(e.getCause)
            None
        }
        case _ => None
      }
    }
  }

}
