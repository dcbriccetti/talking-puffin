package org.talkingpuffin.twitter

import _root_.scala.swing.event.Event
import _root_.scala.swing.Publisher
import _root_.scala.xml.{NodeSeq, Node}
import java.beans.{PropertyChangeEvent, PropertyChangeSupport, PropertyChangeListener}
import javax.swing.{SwingWorker, Timer}
import java.awt.event.{ActionEvent, ActionListener}
import org.apache.log4j.Logger;

object TweetsProvider {
  val CLEAR_EVENT = "clear"
  val NEW_TWEETS_EVENT = "tweets"
}

/**
 * Provides tweets
 */

class TweetsProvider(session: AuthenticatedSession, startingId: Option[Int], providerName: String) {
  private val log = Logger.getLogger("TweetsProvider " + providerName)
  val propChg = new PropertyChangeSupport(this)
  protected var highestId:Option[Int] = startingId

  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private var timer: Timer = _
  
  def addPropertyChangeListener   (l: PropertyChangeListener) = propChg.addPropertyChangeListener(l)
  def removePropertyChangeListener(l: PropertyChangeListener) = propChg.removePropertyChangeListener(l)
  
  //def getUrlFile = "statuses/friends_timeline.xml"
  //def getUrl = urlHost + getUrlFile + "?count=200" + buildSinceParm
  
  protected def buildSinceParm(args: TwitterArgs) = highestId match {
    case None => args
    case Some(i) => args.since(i)
  }
  protected def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.getFriendsTimeline
  def getHighestId = highestId

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequency: Int) {
    this.updateFrequency = updateFrequency * 1000
    if (timer != null && timer.isRunning) {
      timer.stop
    }

    if (updateFrequency > 0) {
      createLoadTimer
    }
  }

  private def createLoadTimer {
    timer = new Timer(updateFrequency, new ActionListener() {
      def actionPerformed(event: ActionEvent) = loadNewData
    })
    timer.start
  }
  
  def loadNewData = {
    loadData(session.user,buildSinceParm(TwitterArgs.maxResults(200)),false)
  }
  
  def loadLastBlockOfTweets = {
    loadData(session.user,TwitterArgs.maxResults(200),true)
  }
  
  private def loadData(username: String, args: TwitterArgs, clear: Boolean): Unit = {
    new SwingWorker[Option[List[TwitterStatus]], Object] {
      val sendClear = clear
      override def doInBackground: Option[List[TwitterStatus]] = {
        try{
          Some(updateFunc(args))
        } catch {
          case e => log.error(e); None
        }
      }
      override def done = {
        get match {
          case Some(statuses) => {
            if (statuses.length > 0) {
              if (sendClear) 
                propChg.firePropertyChange(new PropertyChangeEvent(TweetsProvider.this, 
                  TweetsProvider.CLEAR_EVENT, null, null))
              propChg.firePropertyChange(new PropertyChangeEvent(TweetsProvider.this, 
                TweetsProvider.NEW_TWEETS_EVENT, null, statuses))
            }
          }
          case None => // Ignore
        }
      }
    }.execute
  }
  
}

class MentionsProvider(session: AuthenticatedSession, startingId: Option[Int])
    extends TweetsProvider(session, startingId, "Mentions") {
    override def updateFunc = session.getReplies
}

case class TweetsArrived(tweets: NodeSeq) extends Event
