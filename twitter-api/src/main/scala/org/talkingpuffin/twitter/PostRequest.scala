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
  
  def follow  (screenName: String) = befriend(screenName, "follow")
  def unfollow(screenName: String) = befriend(screenName, "unfollow")
  def block(screenName: String) = befriend(screenName, "block")
  def unblock(screenName: String) = befriend(screenName, "unblock")

  def befriend(screenName: String, verb: String): Node = {

    val (subject, verb2) = verb match {
      case "follow" => ("friendships","create")
      case "unfollow" =>("friendships","delete")
      case "block" => ("blocks","create")
      case "unblock" =>("blocks","delete")
    }

    val url = urlHost + subject + "/" + verb + "/" + screenName + ".xml?id=" + screenName
    processUrl(url)
  }
}
