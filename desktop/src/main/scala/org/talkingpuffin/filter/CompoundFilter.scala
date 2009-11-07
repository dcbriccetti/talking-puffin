package org.talkingpuffin.filter

import org.talkingpuffin.twitter.TwitterStatus
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.filter.RetweetDetector._

/**
 * Used to match a tweet against a set of TextFilters and retweet options.
 */
case class CompoundFilter(val textFilters: List[TextFilter], 
    val retweet: Option[Boolean], val commentedRetweet: Option[Boolean]) 
    extends Loggable {

  /**
   * Whether the specified tweet matches all the elements of this CompoundFilter.
   */
  def matches(status: TwitterStatus): Boolean = {

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

