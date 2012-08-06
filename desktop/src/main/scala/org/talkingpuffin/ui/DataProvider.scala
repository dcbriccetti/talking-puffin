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
import org.talkingpuffin.apix.PageHandler._
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
  private var updateFrequencyMs = DataProvidersDialog.DefaultRefreshSecs * 1000
  
  private var timer: Option[Timer] = None
  
  def getHighestId = highestId
  
  def isActive = timer.exists(_.isRunning)

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequencySecs: Int) {
    updateFrequencyMs = updateFrequencySecs * 1000
    timer.foreach(t => restartTimer())
  }

  def loadContinually() {
    loadNewDataInternal()
    restartTimer()
  }
  
  def loadAndPublishData(paging: Paging, clear: Boolean) {
    longOpListener.startOperation
    new SwingWorker[List[Status], Object] {
      override def doInBackground(): List[Status] = {
        val data = updateFunc(paging)
        highestId = computeHighestId(data, getHighestId)
        data
      }

      override def done() {
        longOpListener.stopOperation
        doAndHandleError(() => get match {
          case Nil =>
          case statuses => DataProvider.this.publish(NewTwitterDataEvent(statuses, clear))
        }, "Error fetching " + providerName + " data for " + tw.getScreenName, session)
      }
    }.execute()
  }

  def stop() {
    timer.foreach(_.stop())
  }
  
  def loadAllAvailable() {
    loadAndPublishData(newPagingMaxPer(), clear = true)
  }

  def getResponseId(response: Status): Long

  protected def newPaging(sinceId: Option[Long] = getHighestId): Paging = {
    val paging = newPagingMaxPer()
    sinceId.foreach(paging.setSinceId)
    paging
  }

  def updateFunc(paging: Paging): List[Status]

  private def computeHighestId(tweets: List[Status], maxId: Option[Long]): Option[Long] = tweets match {
    case tweet :: rest =>
      maxId match {
        case Some(id) =>
          computeHighestId(rest, Some(
            if (getResponseId(tweet) > id)
              getResponseId(tweet)
            else
              id
          ))
        case None =>
          computeHighestId(rest,Some(getResponseId(tweet)))
      }
    case Nil =>
      maxId
  }

  private def restartTimer() {
    def publishNextLoadTime() {
      publish(NextLoadAt(new DateTime((new Date).getTime + updateFrequencyMs)))
    }

    timer.filter(_.isRunning).foreach(_.stop())
  
    if (updateFrequencyMs > 0) {
      val t = new Timer(updateFrequencyMs, new ActionListener() {
        def actionPerformed(event: ActionEvent) {
          loadNewDataInternal()
          publishNextLoadTime()
        }
      })
      t.start()
      timer = Some(t)
      publishNextLoadTime()
    }
    
  }

  private def loadNewDataInternal() {
    loadAndPublishData(newPaging(highestId), clear = false)
  }
}

case class NextLoadAt(when: DateTime) extends Event

abstract class BaseProvider(val providerName: String) extends Publisher {
  def loadContinually()
  def loadAllAvailable()
  def setUpdateFrequency(updateFrequencySecs: Int)
}
