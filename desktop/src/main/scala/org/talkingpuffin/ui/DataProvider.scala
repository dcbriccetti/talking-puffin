package org.talkingpuffin.ui

import apache.log4j.Logger
import java.beans.{PropertyChangeSupport, PropertyChangeListener, PropertyChangeEvent}
import twitter.{AuthenticatedSession, TwitterArgs}
import java.awt.event.{ActionListener, ActionEvent}
import util.TitleCreator
import javax.swing.{Timer, SwingWorker}

abstract class DataProvider(session: AuthenticatedSession, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends BaseProvider(providerName) {
  private val log = Logger.getLogger("TweetsProvider " + providerName)
  private var highestId: Option[Long] = startingId
  val titleCreator = new TitleCreator(providerName)

  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = DataProvidersDialog.DefaultRefreshSecs * 1000;
  
  private var timer: Timer = _
  
  def getHighestId = highestId

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequencySecs: Int) {
    updateFrequency = updateFrequencySecs * 1000
    restartTimer
  }

  def loadNewData {
    loadNewDataInternal
    restartTimer
  }
  
  def loadLastBlockOfTweets {
    loadData(session.user,TwitterArgs.maxResults(200),true)
  }

  type TwitterDataWithId = {def id: Long} 

  def loadData(username: String, args: TwitterArgs, clear: Boolean): Unit = {
    longOpListener.startOperation
    new SwingWorker[Option[List[TwitterDataWithId]], Object] {
      val sendClear = clear
      override def doInBackground: Option[List[TwitterDataWithId]] = {
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

  def getResponseId(response: TwitterDataWithId): Long

  private def computeHighestId(tweets: List[TwitterDataWithId], maxId: Option[Long]):Option[Long] = tweets match {
    case tweet :: rest => maxId match {
        case Some(id) => computeHighestId(rest, if (getResponseId(tweet) > id) Some(getResponseId(tweet)) else Some(id))
        case None => computeHighestId(rest,Some(getResponseId(tweet)))
    }
    case Nil => maxId
  }

  def updateFunc:(TwitterArgs) => List[TwitterDataWithId]
  
  private def addSince(args: TwitterArgs) = highestId match {
    case None => args
    case Some(i) => args.since(i)
  }
  
  private def restartTimer {
    if (timer != null && timer.isRunning) {
      timer.stop
    }
  
    if (updateFrequency > 0) {
      timer = new Timer(updateFrequency, new ActionListener() {
        def actionPerformed(event: ActionEvent) = loadNewDataInternal
      })
      timer.start
    }
  }

  private def loadNewDataInternal {
    val args = addSince(TwitterArgs.maxResults(200))
    log.info("loading new data with args " + args)
    loadData(session.user, args, false)
  }
}

abstract class BaseProvider(val providerName: String) {
  val propChg = new PropertyChangeSupport(this)
  def addPropertyChangeListener   (l: PropertyChangeListener) = propChg.addPropertyChangeListener(l)
  def removePropertyChangeListener(l: PropertyChangeListener) = propChg.removePropertyChangeListener(l)
  def loadNewData
  def loadLastBlockOfTweets
  def setUpdateFrequency(updateFrequencySecs: Int)
}

