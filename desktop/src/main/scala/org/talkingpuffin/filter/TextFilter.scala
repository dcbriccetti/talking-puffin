package org.talkingpuffin.filter

import org.talkingpuffin.twitter.TwitterStatus
import org.talkingpuffin.ui.LinkExtractor

/**
 * A base class for filters that match based on a string or regular expression.
 * <code>getCompareWith</code> is a function that, given a TwitterStatus, 
 * returns a String containing the value from that TwitterStatus that is
 * used for comparisons.
 */
sealed abstract class TextFilter (val text: String, val isRegEx: Boolean, 
    val getCompareWith: (TwitterStatus) => String) {
  override def toString = List(getClass.getName, text, isRegEx.toString).mkString("↓")
}

/**
 * From filter.
 */
case class FromTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.user.screenName)

/**
 * Text (the text of the tweet) filter.
 */
case class TextTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.text)

/**
 * To filter.
 */
case class ToTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, 
    (status) => LinkExtractor.getReplyToInfo(status.inReplyToStatusId, status.text) match {
      case Some(screenNameAndId) => screenNameAndId._1
      case _ => ""
    })

/**
 * Source (the application that created the tweet) filter.
 */
case class SourceTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.sourceName) 

