package org.talkingpuffin.filter

import twitter4j.Status
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.filter.RetweetDetector._

/**
 * Used to match a tweet against a set of TextFilters and retweet options.
 */
case class CompoundFilter(textFilters: List[TextFilter],
    retweet: Option[Boolean], commentedRetweet: Option[Boolean])
    extends Loggable {

  /**
   * Whether the specified tweet matches all the elements of this CompoundFilter.
   */
  def matches(status: Status): Boolean = {

    def allTextFiltersMatch = textFilters.forall(tf => {
      val text = tf.text
      val compareWith = tf.getCompareWith(status)
      
      if (tf.isRegEx)
        text.r.findFirstIn(compareWith).isDefined
      else
        compareWith.toUpperCase.contains(text.toUpperCase)
    })

    def retweetFilterMatches = booleanFilterMatches(retweet, status.isRetweet)

    def commentedRetweetFilterMatches = booleanFilterMatches(
        commentedRetweet, status.isCommentedRetweet)

    def booleanFilterMatches(filter: Option[Boolean], value: Boolean) = filter match {
      case Some(f) => f == value
      case _ => true
    }

    allTextFiltersMatch && retweetFilterMatches && commentedRetweetFilterMatches
  }
  
  override def toString = List(textFilters.map(_.toString).mkString("⇅"), 
    retweet.toString, commentedRetweet.toString).mkString("↕")
}

object CompoundFilter {
  def muteRtSender(sender: String) = 
      CompoundFilter(List(FromTextFilter(sender, isRegEx = false)), Some(true), None)

  def muteCRtSender(sender: String) = 
      CompoundFilter(List(FromTextFilter(sender, isRegEx = false)), None, Some(true))
  
  def muteApp(app: String) = CompoundFilter(List(SourceTextFilter(app, isRegEx = false)), None, None)
  
  def muteSender(sender: String) = CompoundFilter(List(FromTextFilter(sender, isRegEx = false)), None, None)
}
