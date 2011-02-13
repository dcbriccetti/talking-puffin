package org.talkingpuffin.ui

import java.util.Date
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{Timer, SwingWorker}
import swing.event.Event
import swing.Publisher
import org.apache.log4j.Logger
import org.joda.time.DateTime
import util.TitleCreator
import org.talkingpuffin.Session
import org.talkingpuffin.twitter.Constants
import twitter4j.{Status, Paging}

abstract class DataProvider(session: Session, startingId: Option[Long],
    providerName: String, longOpListener: LongOpListener,
    statusTableModelCust: Option[StatusTableModelCust.Value] = None) extends BaseProvider(providerName)
    with Publisher with ErrorHandler {
  
  protected val tw = session.twitter
  private val log = Logger.getLogger(providerName + " " + tw.getScreenName)
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
  
  def loadAndPublishData(paging: Paging, clear: Boolean): Unit = {
    longOpListener.startOperation
    new SwingWorker[List[Status], Object] {
      override def doInBackground: List[Status] = {
        val data = updateFunc(paging)
        highestId = computeHighestId(data, getHighestId)
        data
      }
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => get match {
          case Nil =>
          case statuses => DataProvider.this.publish(NewTwitterDataEvent(statuses, clear))
        }, "Error fetching " + providerName + " data for " + tw.getScreenName, session)
      }
    }.execute
  }

  def stop: Unit = {
    if (timer != null) 
      timer.stop
  }
  
  def loadAllAvailable() = loadAndPublishData(newPagingMaxPer(), true)

  def getResponseId(response: Status): Long

  protected def newPagingMaxPer(): Paging = new Paging(1, Constants.MaxItemsPerRequest)

  protected def newPaging(sinceId: Option[Long] = getHighestId): Paging = {
    val paging = newPagingMaxPer
    sinceId.foreach(paging.setSinceId)
    paging
  }

  def updateFunc(paging: Paging): List[Status]

  private def computeHighestId(tweets: List[Status], maxId: Option[Long]):Option[Long] = tweets match {
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

  private def loadNewDataInternal = loadAndPublishData(newPaging(highestId), false)
}

case class NextLoadAt(when: DateTime) extends Event

abstract class BaseProvider(val providerName: String) extends Publisher {
  def loadContinually()
  def loadAllAvailable()
  def setUpdateFrequency(updateFrequencySecs: Int)
}

