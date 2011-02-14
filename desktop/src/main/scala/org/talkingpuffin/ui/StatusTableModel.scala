package org.talkingpuffin.ui

import javax.swing.Icon
import _root_.scala.swing.event.Event
import _root_.scala.swing.{Reactor, Publisher}
import org.apache.log4j.Logger
import org.talkingpuffin.filter.{FilterSet, FilterSetChanged, TagUsers}
import org.talkingpuffin.state.GlobalPrefs.PrefChangedEvent
import org.talkingpuffin.state.{GlobalPrefs, PrefKeys}
import org.talkingpuffin.ui.table.{EmphasizedString, StatusCell}
import util.DesktopUtil
import org.talkingpuffin.Session
import org.talkingpuffin.util.Loggable
import twitter4j.{User, DirectMessage, Status}
import org.talkingpuffin.twitter.RichStatus._

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(session: Session, val options: StatusTableOptions, val tweetsProvider: BaseProvider,
    val relationships: Relationships, screenNameToUserNameMap: Map[String, String], filterSet: FilterSet,
    val tagUsers: TagUsers) 
    extends UserAndStatusProvider with TaggingSupport with Publisher with Reactor with Loggable
{
  private val username = session.twitter.getScreenName
  private val log = Logger.getLogger("StatusTableModel " + tweetsProvider.providerName + " " + username)

  val unessentialCols = List("When", "Image", "From", "To") // Can be quickly hidden
  
  private val userPrefs = GlobalPrefs.prefsForUser(session.serviceName, username)

  /** All loaded statuses */
  private var statuses = List[Status]()
  
  /** Statuses, after filtering */
  private var filteredStatuses_ = Array[Status]()
  def filteredStatuses = filteredStatuses_
  
  var preChangeListener: PreChangeListener = _;
  
  listenTo(filterSet)
  reactions += { case FilterSetChanged(s) => filterAndNotify }

  listenTo(GlobalPrefs.publisher)
  reactions += { case e: PrefChangedEvent => 
    if (e.key == PrefKeys.SHOW_TWEET_DATE_AS_AGE) fireTableDataChanged}  

  listenTo(tweetsProvider)
  reactions += { 
    case e: NewTwitterDataEvent => {
      val newTweets = e.data
      log.info("Tweets Arrived: " + newTweets.length)
      if (e.clear) clear(true)
      processStatuses(newTweets)
      if (GlobalPrefs.isOn(PrefKeys.NOTIFY_TWEETS)) doNotify(newTweets)
    }
  }
  
  def getColumnCount = 5
  def getRowCount = filteredStatuses_.length
  override def getColumnName(column: Int) = (unessentialCols ::: List("Status"))(column)

  private val pictureCell = new PictureCell(this, 0)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val topStatus = getStatusAt(rowIndex)
    val status = topStatus.retweetOrTweet
    val parent = if (topStatus.isRetweet) Some(topStatus) else None

    def senderName(status: Status) =
      if (GlobalPrefs.isOn(PrefKeys.USE_REAL_NAMES)) 
        UserProperties.overriddenUserName(userPrefs, status.getUser)
      else status.getUser.getScreenName

    def senderNameEs(status: Status): EmphasizedString =
      new EmphasizedString(Some(senderName(status)), relationships.followerIds.contains(status.getUser.getId))

    def toName(status: Status) = status.inReplyToScreenName match {
      case Some(screenName) => Some(
        if (GlobalPrefs.isOn(PrefKeys.USE_REAL_NAMES)) {
          screenNameToUserNameMap.getOrElse(screenName, screenName)
        } else {
          screenName
        })
      case None => None
    }
    
    columnIndex match {
      case 0 => status.createdAt.toDate
      case 1 => pictureCell.request(status.getUser.getProfileImageURL.toString, rowIndex)
      case 2 => senderNameEs(status)
      case 3 => new EmphasizedString(toName(status), false)
      case 4 => 
        var st = getStatusText(status, username, parent)
        if (options.showToColumn) st = LinkExtractor.getWithoutUser(st)
        StatusCell(if (options.showAgeColumn) None else Some(status.createdAt.toDate),
          if (showNameInStatus) Some(senderNameEs(status)) else None, st)
    }
  }
  
  protected def showNameInStatus = ! options.showNameColumn
  
  def getStatusText(status: Status, username: String, parent: Option[Status]): String = {
    status.getText + (if (parent.isDefined) " RT by " + parent.get.getUser.getScreenName else "")
  }

  def getStatusAt(rowIndex: Int): Status = filteredStatuses_(rowIndex)
  
  def getUserAndStatusAt(rowIndex: Int): UserAndStatus = {
    val status = getStatusAt(rowIndex)
    UserAndStatus(status.getUser,
      if (status.retweet.isDefined) Some(status.retweet.get.getUser) else None, Some(status))
  }

  override def getColumnClass(col: Int) = List(
    classOf[java.util.Date], 
    classOf[Icon], 
    classOf[String],
    classOf[String], 
    classOf[StatusCell])(col) 
  
  def getIndexOfStatus(statusId: Long): Option[Int] = 
    filteredStatuses_.zipWithIndex.find(si => si._1.getId == statusId) match {
      case Some((_, i)) => Some(i)
      case None => None
    }

  private def mapToIdTuple(users: List[User]) = users.map(user => (user.getId, user))
  
  def muteSelectedUsers(rows: List[Int]) = {
    filterSet.adder.muteSenders(getScreenNames(rows))
    filterAndNotify
  }

  def muteSelectedSenderReceivers(rows: List[Int], andViceVersa: Boolean) = {
    val senderReceivers = for {
      row <- rows
      status = filteredStatuses_(row)
      sender = status.getUser.getScreenName
      if status.inReplyToScreenName.isDefined
    } yield (sender, status.inReplyToScreenName.get)
    
    filterSet.adder.muteSenderReceivers(senderReceivers)
    if (andViceVersa)
      filterSet.adder.muteSenderReceivers(senderReceivers map (t => (t._2, t._1)))
    filterAndNotify
  }

  def muteSelectedUsersRetweets(rows: List[Int]) = {
    filterSet.adder.muteRetweetUsers(getScreenNames(rows))
    filterAndNotify
  }

  def muteSelectedUsersCommentedRetweets(rows: List[Int]) = {
    filterSet.adder.muteSelectedUsersCommentedRetweets(getScreenNames(rows))
    filterAndNotify
  }

  def muteSelectedApps(rows: List[Int]) = {
    filterSet.adder.muteApps(rows.map(i => filteredStatuses_(i).sourceName))
    filterAndNotify
  }

  def getUsers(rows: List[Int]): List[UserIdName] = rows.map(i => {
    val user = filteredStatuses_(i).getUser
    new UserIdName(user.getId, user.getName)
  })
  
  private def getScreenNames(rows: List[Int]) = rows.map(i => filteredStatuses_(i).getUser.getScreenName)
  
  def getStatuses(rows: List[Int]): List[Status] = rows.map(filteredStatuses_)

  private def processStatuses(newStatuses: List[Status]) {
    statuses :::= newStatuses.reverse
    filterAndNotify
  }
  
  /**
   * Clear (remove) statuses
   */
  def clear(all: Boolean) {
    statuses = if (all) List[Status]() else statuses.filter(! filteredStatuses_.contains(_))
    filterAndNotify
  }
  
  def removeStatuses(indexes: List[Int]) {
    val deleteStatuses = getStatuses(indexes)
    statuses = statuses.filter(! deleteStatuses.contains(_))
    filterAndNotify
  }
  
  def removeStatusesFrom(screenNames: List[String]) {
    statuses = statuses.filter(s => ! screenNames.contains(s.getUser.getScreenName))
    filterAndNotify
  }

  private def filterAndNotify {
    if (preChangeListener != null) {
      preChangeListener.tableChanging
    }
    
    filteredStatuses_ = filterSet.filter(statuses, relationships).toArray 
    publish(new TableContentsChanged(this, filteredStatuses_.length, statuses.length))
    fireTableDataChanged
  }
  
/* todo private def adaptDmsToTweets(dms: List[DirectMessage]): List[Status] = {
    dms.map(dm => new Status {
      text = dm.getText
      user = if (dm.sender.getScreenName == username) dm.recipient else dm.sender
      id = dm.getId
      createdAt = dm.getCreatedAt
    })
  }
  */

  def doNotify(newTweets: List[Status]) = newTweets.length match {
    case 1 => DesktopUtil.notify(newTweets.first.getUser.getScreenName+": "+newTweets.first.getText,"New tweet")
    case _ => DesktopUtil.notify(newTweets.length +" new tweets arrived","New tweets")
  }
}

/**
 * Provide hook before the model fires an update notification,
 * so that the currently selected rows can be saved.
 */
trait PreChangeListener {
  def tableChanging
}

case class TableContentsChanged(model: StatusTableModel, filteredIn: Int, total: Int) extends Event
  
trait Mentions extends StatusTableModel {
  override def getStatusText(status: Status, username: String, parent: Option[Status]): String = {
    val text = status.getText
    val userTag = "@" + username
    if (text.startsWith(userTag)) text.substring(userTag.length).trim else text
  }
}

trait DmsSent extends StatusTableModel {
  override def getValueAt(rowIndex: Int, columnIndex: Int) = super.getValueAt(rowIndex, 
    List(0,1,2,2,4)(columnIndex))
  override def showNameInStatus = false
}

