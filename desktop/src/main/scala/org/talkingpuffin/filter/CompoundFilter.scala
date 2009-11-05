package org.talkingpuffin.filter

import org.talkingpuffin.twitter.TwitterStatus
import java.util.regex.Pattern
import swing.Publisher
import swing.event.Event
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.filter.RetweetDetector._
import org.talkingpuffin.ui.LinkExtractor

class CompoundFilters extends Publisher {
  var list = List[CompoundFilter]()
  def clear = list = List[CompoundFilter]()
  def matchesAll(status: TwitterStatus): Boolean = list.forall(_.matches(status))
  def matchesAny(status: TwitterStatus): Boolean = list.exists(_.matches(status))
  def add(cf: CompoundFilter) = {
    list = list ::: List(cf)
    publish
  }
  def publish: Unit = publish(new CompoundFiltersChanged)
  override def toString = list.map(_.toString).mkString("↑")
}

class CompoundFiltersChanged extends Event

case class CompoundFilter(val textFilters: List[TextFilter], val retweet: Option[Boolean], 
    val commentedRetweet: Option[Boolean]) extends Loggable {

  def matches(status: TwitterStatus): Boolean = {
    textFilters.foreach(tf => {
      val elementMatches = if (tf.isRegEx)
        Pattern.compile(tf.text).matcher(tf.getCompareWith(status)).find
      else
        tf.getCompareWith(status).toUpperCase.contains(tf.text.toUpperCase)
      if (! elementMatches) {
        return false
      }
    })
    (retweet match {
      case Some(rt) if rt => status.isRetweet
      case _ => true
    }) && 
    (commentedRetweet match {
      case Some(rt) if rt => status.isCommentedRetweet
      case _ => true
    })  
  }
  
  override def toString = 
    List(textFilters.map(_.toString).mkString("⇅"), retweet.toString, commentedRetweet.toString).mkString("↕")
}

sealed abstract case class TextFilter (val text: String, val isRegEx: Boolean, 
    getCompareWith: (TwitterStatus) => String) {
  override def toString = List(getClass.getName, text, isRegEx.toString).mkString("↓")
}

case class FromTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.user.screenName)

case class TextTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.text)

case class ToTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, 
    (status) => LinkExtractor.getReplyToInfo(status.inReplyToStatusId, status.text) match {
      case Some(screenNameAndId) => screenNameAndId._1
      case _ => ""
    }) 

case class SourceTextFilter(override val text: String, override val isRegEx: Boolean) 
    extends TextFilter(text, isRegEx, (status) => status.sourceName) 

