package org.talkingpuffin.filter

import org.talkingpuffin.twitter.TwitterStatus
import java.util.regex.Pattern
import swing.Publisher
import swing.event.Event
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.filter.RetweetDetector._

class CompoundFilters extends Publisher {
  var list = List[CompoundFilter]()
  def clear = list = List[CompoundFilter]()
  def matchesAll(status: TwitterStatus): Boolean = list.forall(_.matches(status))
  def matchesAny(status: TwitterStatus): Boolean = list.exists(_.matches(status))
  def add(cf: CompoundFilter) = {
    list :::= List(cf)
    publish
  }
  def publish: Unit = publish(new CompoundFiltersChanged)
}

class CompoundFiltersChanged extends Event

case class CompoundFilter(val from: Option[FromTextFilter], val text: Option[TextTextFilter], 
    val to: Option[ToTextFilter], val source: Option[SourceTextFilter], val retweet: Option[Boolean]) 
    extends Loggable {

  def getActive: List[TextFilter] = {
    for {
      tf <- List(from, text, to, source)
      if tf.isDefined
    } yield tf.get
  }
  
  def matches(status: TwitterStatus): Boolean = {
    getActive.foreach(tf => {
      val elementMatches = if (tf.isRegEx)
        Pattern.compile(tf.text).matcher(tf.compareWith(status)).find
      else
        tf.compareWith(status).toUpperCase.contains(tf.text.toUpperCase)
      if (! elementMatches) {
        return false
      }
    })
    retweet match {
      case Some(rt) if rt => status.isRetweet
      case _ => true
    }
  }
}

sealed abstract case class TextFilter (val text: String, val isRegEx: Boolean, compareWith: (TwitterStatus) => String)

case class FromTextFilter(override val text: String, override val isRegEx: Boolean) 
        extends TextFilter(text, isRegEx, (status) => status.user.screenName) 
case class TextTextFilter(override val text: String, override val isRegEx: Boolean) 
        extends TextFilter(text, isRegEx, (status) => status.text) 
case class ToTextFilter(override val text: String, override val isRegEx: Boolean) 
        extends TextFilter(text, isRegEx, (status) => status.inReplyToStatusId.toString) // TODO 
case class SourceTextFilter(override val text: String, override val isRegEx: Boolean) 
        extends TextFilter(text, isRegEx, (status) => status.sourceName) 

