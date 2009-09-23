package org.talkingpuffin.ui

import _root_.scala.swing.event.Event
import _root_.scala.xml.{NodeSeq, Node}
import java.beans.{PropertyChangeEvent, PropertyChangeSupport, PropertyChangeListener}
import javax.swing.{SwingWorker, Timer}
import java.awt.event.{ActionEvent, ActionListener}
import org.apache.log4j.Logger
import talkingpuffin.util.Loggable
import twitter.{TwitterMessage, AuthenticatedSession, TwitterArgs, TwitterStatus}
import util.TitleCreator

object TweetsProvider {
  val CLEAR_EVENT = "clear"
  val NEW_TWEETS_EVENT = "tweets"
}

abstract class BaseProvider(val providerName: String) {
  val propChg = new PropertyChangeSupport(this)
  def addPropertyChangeListener   (l: PropertyChangeListener) = propChg.addPropertyChangeListener(l)
  def removePropertyChangeListener(l: PropertyChangeListener) = propChg.removePropertyChangeListener(l)
  def loadNewData
  def loadLastBlockOfTweets
  def setUpdateFrequency(updateFrequencySecs: Int)
}

/**
 * Provides tweets
 */
abstract class DataProvider[T](session: AuthenticatedSession, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends BaseProvider(providerName) {
  private val log = Logger.getLogger("TweetsProvider " + providerName)
  protected var highestId:Option[Long] = startingId
  val titleCreator = new TitleCreator(providerName)

  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private var timer: Timer = _
  
  protected def buildSinceParm(args: TwitterArgs) = highestId match {
    case None => args
    case Some(i) => args.since(i)
  }
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

  def loadNewData = {
    val args = buildSinceParm(TwitterArgs.maxResults(200))
    log.info("loading new data with args " + args)
    loadData(session.user,args,false)
  }
  
  def loadLastBlockOfTweets = {
    loadData(session.user,TwitterArgs.maxResults(200),true)
  }

  def loadData(username: String, args: TwitterArgs, clear: Boolean): Unit = {
    longOpListener.startOperation
    new SwingWorker[Option[List[T]], Object] {
      val sendClear = clear
      override def doInBackground: Option[List[T]] = {
        try{
          val statuses = Some(updateFunc(args))
          statuses match {
            case Some(tweets) => highestId = computeHighestId(tweets,getHighestId)
          }
          log.info("highest tweet id is now " + getHighestId)
          statuses
        } catch {
          case e => error(e.toString); None
        }
      }
      override def done = {
        longOpListener.stopOperation
        get match {
          case Some(statuses) => {
            if (statuses.length > 0) {
              if (sendClear) 
                propChg.firePropertyChange(new PropertyChangeEvent(DataProvider.this, 
                  TweetsProvider.CLEAR_EVENT, null, null))
              propChg.firePropertyChange(new PropertyChangeEvent(DataProvider.this, 
                TweetsProvider.NEW_TWEETS_EVENT, null, statuses))
            }
          }
          case None => // Ignore
        }
      }
    }.execute
  }

  def getResponseId(response: T): Long

  private def computeHighestId(tweets: List[T], maxId: Option[Long]):Option[Long] = tweets match {
    case tweet :: rest => maxId match {
        case Some(id) => computeHighestId(rest, if (getResponseId(tweet) > id) Some(getResponseId(tweet)) else Some(id))
        case None => computeHighestId(rest,Some(getResponseId(tweet)))
    }
    case Nil => maxId
  }

  def updateFunc:(TwitterArgs) => List[T]
  
}

abstract class TweetsProvider(session: AuthenticatedSession, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider[TwitterStatus](session, startingId, providerName, longOpListener) with Loggable {
  override def getResponseId(response: TwitterStatus): Long = response.id
}

class FollowingProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.getFriendsTimeline
}

class MentionsProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.getReplies
}

class DmsReceivedProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider[TwitterMessage](session, startingId, "DMs Rcvd", longOpListener) {
  def updateFunc:(TwitterArgs) => List[TwitterMessage] = session.getDirectMessages
  override def getResponseId(response: TwitterMessage): Long = response.id
}

class DmsSentProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider[TwitterMessage](session, startingId, "DMs Sent", longOpListener) {
  def updateFunc:(TwitterArgs) => List[TwitterMessage] = session.getSentMessages
  override def getResponseId(response: TwitterMessage): Long = response.id
}

case class TweetsArrived(tweets: NodeSeq) extends Event
