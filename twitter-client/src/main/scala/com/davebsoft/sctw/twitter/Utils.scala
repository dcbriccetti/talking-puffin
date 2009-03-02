package com.davebsoft.sctw.twitter

/**
 * Utiity methods
 * @author Dave Briccetti
 */

object Utils {
  
  /**
   * Returns the Twitter handle of the user whose @handle appears at the beginning of 
   * the tweet, or an empty string.
   */
  def getReplyToUser(text: String): String = {
    val m = java.util.regex.Pattern.compile("^@(\\S+)").matcher(text)
    if (m.find) m.group(1) else ""
  }
}