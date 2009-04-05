package com.davebsoft.sctw.twitter

import _root_.scala.xml.NodeSeq

/**
 * Helper methods related to statuses
 * @author Dave Briccetti
 */

object StatusUtil {
  def getScreenNameFromStatus(status: NodeSeq) = (status \ "user" \ "screen_name").text
  
}