package org.talkingpuffin.twitter

import _root_.scala.xml.NodeSeq

/**
 * Helper methods related to statuses
 */

class Status(status: NodeSeq) {
  def getScreenNameFromStatus = (status \ "user" \ "screen_name").text
  
}