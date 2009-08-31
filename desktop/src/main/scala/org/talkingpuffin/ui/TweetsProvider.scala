package org.talkingpuffin.ui

import _root_.scala.swing.event.Event
import _root_.scala.xml.{NodeSeq, Node}
import java.beans.{PropertyChangeEvent, PropertyChangeSupport, PropertyChangeListener}
import javax.swing.{SwingWorker, Timer}
import java.awt.event.{ActionEvent, ActionListener}
import org.apache.log4j.Logger
import twitter.{AuthenticatedSession, TwitterArgs, TwitterStatus}


object TweetsProvider {
  val CLEAR_EVENT = "clear"
  val NEW_TWEETS_EVENT = "tweets"
}

/**
 * Provides tweets
 */
abstract class TweetsProvider(session: AuthenticatedSession, startingId: Option[Long], 
    val providerName: String, longOpListener: LongOpListener) {
  private val log = Logger.getLogger("TweetsProvider " + providerName)
  val propChg = new PropertyChangeSupport(this)
  protected var highestId:Option[Long] = startingId

  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private var timer: Timer = _
  
  def addPropertyChangeListener   (l: PropertyChangeListener) = propChg.addPropertyChangeListener(l)
  def removePropertyChangeListener(l: PropertyChangeListener) = propChg.removePropertyChangeListener(l)
  
  protected def buildSinceParm(args: TwitterArgs) = highestId match {
    case None => args
    case Some(i) => args.since(i)
  }
  protected def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.getFriendsTimeline
  def getHighestId = highestId

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequencySecs: Int) {
    this.updateFrequency = updateFrequencySecs * 1000
    if (timer != null && timer.isRunning) {
      timer.stop
    }

    if (updateFrequency > 0) {
      timer = new Timer(updateFrequency, new ActionListener() {
        def actionPerformed(event: ActionEvent) = loadNewData
      })
      timer.start
    }
  }

  private def computeHighestId(tweets: List[TwitterStatus], maxId: Option[Long]):Option[Long] = tweets match {
    case tweet :: rest => maxId match {
        case Some(id) => computeHighestId(rest,
                                           if(tweet.id > id) Some(tweet.id) else Some(id))
        case None => computeHighestId(rest,Some(tweet.id))
    }
    case Nil => maxId
  }

  def loadNewData = {
    val args = buildSinceParm(TwitterArgs.maxResults(200))
    log.info("loading new data with args " + args)
    loadData(session.user,args,false)
  }
  
  def loadLastBlockOfTweets = {
    loadData(session.user,TwitterArgs.maxResults(200),true)
  }

  private def loadData(username: String, args: TwitterArgs, clear: Boolean): Unit = {
    longOpListener.startOperation
    new SwingWorker[Option[List[TwitterStatus]], Object] {
      val sendClear = clear
      override def doInBackground: Option[List[TwitterStatus]] = {
        try{
          val statuses = Some(updateFunc(args))
          statuses match {
            case Some(tweets) => highestId = computeHighestId(tweets,getHighestId)
          }
          log.info("highest tweet id is now " + getHighestId)
          statuses
        } catch {
          case e => log.error(e); None
        }
      }
      override def done = {
        longOpListener.stopOperation
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

class FollowingProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
}

class MentionsProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
    override def updateFunc = session.getReplies
}

case class TweetsArrived(tweets: NodeSeq) extends Event
