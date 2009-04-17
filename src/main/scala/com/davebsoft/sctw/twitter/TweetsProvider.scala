package com.davebsoft.sctw.twitter

import _root_.scala.xml.{NodeSeq, Node}

/**
 * Provides tweets
 * @author Dave Briccetti
 */

class TweetsProvider(username: String, password: String, startingId: String) extends DataProvider {
  setCredentials(username, password)
  protected var highestId = if (startingId != null) startingId else null
  
  def getUrl = "http://twitter.com/statuses/friends_timeline.xml?count=200" +
      (if (highestId == null) "" else "&since_id=" + highestId)
  
  def getHighestId = highestId

  /**
   * Fetches statuses from Twitter.
   */
  def loadTwitterStatusData: NodeSeq = {
    formatData(loadTwitterData)
  }
  
  /**
   * Fetches statuses from Twitter.
   */
  def loadLastSet: NodeSeq = {
    formatData(loadTwitterData("http://twitter.com/statuses/friends_timeline.xml?count=200"))
  }
  
  private def formatData(elem: Node): NodeSeq = {
    if (elem != null) {
      val statuses = elem \\ "status"
      if (statuses.length > 0) {
        highestId = (statuses(0) \ "id").text 
      }
      return statuses
    }
    List[Node]()
  }
}

class RepliesProvider(username: String, password: String) extends TweetsProvider(username, password, null) {
  override def getUrl = "http://twitter.com/statuses/replies.xml"
}

