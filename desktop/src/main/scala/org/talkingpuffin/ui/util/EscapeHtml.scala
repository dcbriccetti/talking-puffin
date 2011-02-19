package org.talkingpuffin.ui.util

import org.apache.commons.lang.StringEscapeUtils

/**
 * Without escaping HTML before setting it into Swing controls, <frameset>, and perhaps
 * other elements, will cause an exception.
 */
object EscapeHtml {
  def apply(s: String) = StringEscapeUtils.escapeHtml(s)
}