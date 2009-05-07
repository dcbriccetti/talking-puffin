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
 * @author Dave Briccetti
 */

class TweetsProvider(username: String, password: String, startingId: Option[String], providerName: String) extends DataProvider {
  private val log = Logger.getLogger("TweetsProvider " + providerName)
  val propChg = new PropertyChangeSupport(this)
  setCredentials(username, password)
  protected var highestId = startingId match {case Some(s) => s case None => null}

  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private var timer: Timer = _
  
  def addPropertyChangeListener   (l: PropertyChangeListener) = propChg.addPropertyChangeListener(l)
  def removePropertyChangeListener(l: PropertyChangeListener) = propChg.removePropertyChangeListener(l)
  
  def getUrlFile = "statuses/friends_timeline.xml"
  def getUrl = urlHost + getUrlFile + "?count=200" + buildSinceParm
  
  protected def buildSinceParm = (if (highestId == null) "" else "&since_id=" + highestId)
  
  def getHighestId = highestId

  private def formatData(elem: Node): NodeSeq = {
    if (elem != null) {
      val statuses = elem \\ "status"
      if (statuses.length > 0) {
        highestId = (statuses(0) \ "id").text 
      }
      return statuses
    }
    List[Node]()
  }

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
  
  def loadNewData = loadData(getUrl, false)
  
  def loadLastBlockOfTweets: Unit = loadData(urlHost + getUrlFile + "?count=200", true)
  
  private def loadData(url: String, clear: Boolean): Unit = {
    new SwingWorker[Option[NodeSeq], Object] {
      val sendClear = clear
      override def doInBackground: Option[NodeSeq] = {
        loadTwitterData(url) match {
          case HttpXMLSuccess(_,_,n) => Some(formatData(n))
          case HttpException(e) => log.error(e); None
          case _ => None
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

class MentionsProvider(username: String, password: String, startingId: Option[String]) 
    extends TweetsProvider(username, password, startingId, "Mentions") {
  override def getUrlFile = "statuses/mentions.xml"
}

case class TweetsArrived(tweets: NodeSeq) extends Event
