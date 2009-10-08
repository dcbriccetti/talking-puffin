package org.talkingpuffin.ui

import java.util.Date
import java.awt.event.{ActionListener, ActionEvent}
import swing.event.Event
import swing.Publisher
import org.apache.log4j.Logger
import util.TitleCreator
import org.talkingpuffin.twitter.{TwitterArgs, AuthenticatedSession}
import org.talkingpuffin.time.TimeFormatter
import org.joda.time.DateTime
import javax.swing.{JOptionPane, Timer, SwingWorker}

abstract class DataProvider(session: AuthenticatedSession, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends BaseProvider(providerName)
    with Publisher {
  private val log = Logger.getLogger("DataProvider " + providerName)
  private var highestId: Option[Long] = startingId
  val titleCreator = new TitleCreator(providerName)

  /** How often, in ms, to fetch and load new data */
  private var updateIntervalMs = DataProvidersDialog.DefaultRefreshSecs * 1000;
  
  private var timer: Timer = _
  
  def getHighestId = highestId

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequencySecs: Int) {
    updateIntervalMs = updateFrequencySecs * 1000
    restartTimer
  }

  def loadNewData {
    loadNewDataInternal
    restartTimer
  }
  
  def loadLastBlockOfTweets = loadData(TwitterArgs.maxResults(200), true)

  type TwitterDataWithId = {def id: Long} 

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
    def publishNext {
      val nextLoadAt = NextLoadAt(new DateTime((new Date).getTime + updateIntervalMs))
      publish(nextLoadAt)
      log.debug("Next load in " + new TimeFormatter(updateIntervalMs / 1000).longForm)
    }

    if (timer != null && timer.isRunning) {
      timer.stop
    }
  
    if (updateIntervalMs > 0) {
      timer = new Timer(updateIntervalMs, new ActionListener() {
        def actionPerformed(event: ActionEvent) = {
          loadNewDataInternal
          publishNext
        }
      })
      timer.start
      publishNext
    }
    
  }

  private def loadNewDataInternal {
    val args = addSince(TwitterArgs.maxResults(200))
    log.info("loading new data with args " + args)
    loadData(args, false)
  }

  private def loadData(args: TwitterArgs, clear: Boolean): Unit = {
    longOpListener.startOperation
    new SwingWorker[List[TwitterDataWithId], Object] {
      val sendClear = clear
      override def doInBackground: List[TwitterDataWithId] = {
        val data = updateFunc(args)
        highestId = computeHighestId(data, getHighestId)
        log.info("highest ID: " + getHighestId.getOrElse(""))
        data
      }
      override def done {
        longOpListener.stopOperation
        try {
          val statuses = get
          if (statuses != Nil)
            DataProvider.this.publish(NewTwitterDataEvent(statuses, sendClear)) // SwingWorker has a publish
        } catch {
          case e: Throwable => {
            val msg = "Error fetching " + providerName + " data for " + session.user + ": " + e.getMessage
            log.error(msg)
            JOptionPane.showMessageDialog(null, msg)
          }
        }
      }
    }.execute
  }

}

case class NextLoadAt(val when: DateTime) extends Event

abstract class BaseProvider(val providerName: String) extends Publisher {
  def loadNewData
  def loadLastBlockOfTweets
  def setUpdateFrequency(updateFrequencySecs: Int)
}

