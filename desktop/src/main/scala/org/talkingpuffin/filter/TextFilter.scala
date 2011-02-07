package org.talkingpuffin.filter

import twitter4j.Status

/**
 * A base class for filters that match based on a string or regular expression.
 * <code>getCompareWith</code> is a function that, given a Status,
 * returns a String containing the value from that Status that is
 * used for comparisons.
 */
sealed abstract class TextFilter (val text: String, val isRegEx: Boolean, 
    val getCompareWith: (Status) => String) {
  override def toString = List(getClass.getName, text, isRegEx.toString).mkString("↓")
}

/**
 * From filter.
 */
case class FromTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.getUser.getScreenName)

/**
 * Text (the text of the tweet) filter.
 */
case class TextTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.getText)

/**
 * To filter.
 */
case class ToTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.getInReplyToScreenName)

/**
 * Source (the application that created the tweet) filter.
 */
case class SourceTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.getSource)

