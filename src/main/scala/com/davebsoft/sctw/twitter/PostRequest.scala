package com.davebsoft.sctw.twitter

import _root_.scala.xml.{XML, Node}
import java.net.URLEncoder

class PostRequest(username: String, password: String) extends HttpHandler {
  setCredentials(username, password)
  
  def processUrl(url: String): Node = {
    val (method, result, responseBody) = doPost(url)

    if (result != 200) {
      println(responseBody)
      throw new DataFetchException(result, responseBody)
    }
    XML.loadString(responseBody)
  }
}

class Sender(username: String, password: String) extends PostRequest(username, password) {
  
  def send(message: String, replyTo: Option[String]): Node = {
    val replyToParm = replyTo match {
        case Some(s) => "&in_reply_to_status_id=" + URLEncoder.encode(s, "UTF-8")
        case None => ""
      }
    val url = "http://twitter.com/statuses/update.xml?source=talkingpuffin&status=" + 
      URLEncoder.encode(message, "UTF-8") + replyToParm 
    
    processUrl(url)
  }
}

/**
 * Unfollows
 * @author Dave Briccetti
 */

class Follower(username: String, password: String) extends PostRequest(username, password) {
  
  def unfollow(screenName: String): Node = {
    val url = "http://twitter.com/friendships/destroy/" + screenName + ".xml?id=" + screenName
    processUrl(url)
  }
}
