package org.talkingpuffin.ui

import java.util.Date
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{Timer, SwingWorker}
import swing.event.Event
import swing.Publisher
import org.apache.log4j.Logger
import org.joda.time.DateTime
import twitter4j.Paging
import util.TitleCreator
import org.talkingpuffin.Session
import org.talkingpuffin.twitter.{TwitterArgs, Constants}

abstract class DataProvider(session: Session, startingId: Option[Long],
    providerName: String, longOpListener: LongOpListener) extends BaseProvider(providerName)
    with Publisher with ErrorHandler {
  
  private val twSess = session.twitterSession
  private val log = Logger.getLogger(providerName + " " + twSess.user)
  private var highestId: Option[Long] = startingId
  val titleCreator = new TitleCreator(providerName)

  /** How often, in ms, to fetch and load new data */
  private var updateFrequencyMs = DataProvidersDialog.DefaultRefreshSecs * 1000;
  
  private var timer: Timer = _
  
  def getHighestId = highestId
  
  def isActive = timer != null && timer.isRunning

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequencySecs: Int) {
    updateFrequencyMs = updateFrequencySecs * 1000
    if (timer != null) 
      restartTimer                                                 
  }

  def loadContinually() {
    loadNewDataInternal
    restartTimer
  }
  
  def loadAndPublishData(args: TwitterArgs, clear: Boolean): Unit = {
    longOpListener.startOperation
    new SwingWorker[List[TwitterDataWithId], Object] {
      val sendClear = clear
      override def doInBackground: List[TwitterDataWithId] = {
        val data = updateFunc(args)
        highestId = computeHighestId(data, getHighestId)
        data
      }
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val statuses = get
          if (statuses != Nil)
            DataProvider.this.publish(NewTwitterDataEvent(statuses, sendClear)) // SwingWorker has a publish
          }, "Error fetching " + providerName + " data for " + twSess.user, session)
      }
    }.execute
  }

  def stop: Unit = {
    if (timer != null) 
      timer.stop
  }
  
  def loadLastBlockOfTweets() = loadAndPublishData(TwitterArgs(None), true)

  type TwitterDataWithId = {def getId: Long}

  def getResponseId(response: TwitterDataWithId): Long

  protected def paging(highestId: Option[Long] = getHighestId): Paging = {
    val paging = new Paging
    paging.setCount(Constants.MaxItemsPerRequest)
    highestId.foreach(paging.setSinceId)
    paging
  }

  def updateFunc(args: TwitterArgs): List[TwitterDataWithId]

  private def computeHighestId(tweets: List[TwitterDataWithId], maxId: Option[Long]):Option[Long] = tweets match {
    case tweet :: rest => maxId match {
      case Some(id) => computeHighestId(rest, if (getResponseId(tweet) > id) Some(getResponseId(tweet)) else Some(id))
      case None => computeHighestId(rest,Some(getResponseId(tweet)))
    }
    case Nil => maxId
  }

  private def restartTimer {
    def publishNextLoadTime = publish(NextLoadAt(new DateTime((new Date).getTime + updateFrequencyMs)))

    if (timer != null && timer.isRunning) {
      timer.stop
    }
  
    if (updateFrequencyMs > 0) {
      timer = new Timer(updateFrequencyMs, new ActionListener() {
        def actionPerformed(event: ActionEvent) = {
          loadNewDataInternal
          publishNextLoadTime
        }
      })
      timer.start
      publishNextLoadTime
    }
    
  }

  private def loadNewDataInternal = loadAndPublishData(TwitterArgs(highestId), false)

}

case class NextLoadAt(when: DateTime) extends Event

abstract class BaseProvider(val providerName: String) extends Publisher {
  def loadContinually()
  def loadLastBlockOfTweets()
  def setUpdateFrequency(updateFrequencySecs: Int)
}

