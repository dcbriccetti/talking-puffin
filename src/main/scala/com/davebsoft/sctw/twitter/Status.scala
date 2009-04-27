package com.davebsoft.sctw.twitter

import _root_.scala.xml.NodeSeq

/**
 * Helper methods related to statuses
 * @author Dave Briccetti
 */

class Status(status: NodeSeq) {
  def getScreenNameFromStatus = (status \ "user" \ "screen_name").text
  
}