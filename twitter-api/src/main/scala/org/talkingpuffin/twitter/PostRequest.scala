package org.talkingpuffin.twitter

import _root_.scala.xml.{XML, Node}
import java.net.URLEncoder

class PostRequest(username: String, password: String) extends HttpHandler {
  var urlHost = "http://twitter.com/" 
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
    val url = urlHost + "statuses/update.xml?source=talkingpuffin&status=" + 
      URLEncoder.encode(message, "UTF-8") + replyToParm 
    
    processUrl(url)
  }
}

/**
 * Unfollows
 * @author Dave Briccetti
 */

class Follower(username: String, password: String) extends PostRequest(username, password) {
  
  def follow  (screenName: String) = befriend(screenName, "create")
  def unfollow(screenName: String) = befriend(screenName, "destroy")
    
  def befriend(screenName: String, verb: String): Node = {
    val url = urlHost + "friendships/" + verb + "/" + screenName + ".xml?id=" + screenName
    processUrl(url)
  }
}
