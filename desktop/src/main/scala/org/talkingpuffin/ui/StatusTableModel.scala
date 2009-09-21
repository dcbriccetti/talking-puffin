package org.talkingpuffin.ui

import _root_.scala.swing.event.Event
import _root_.scala.swing.{Reactor, Publisher}
import filter.{FilterSet, FilterSetChanged, TagUsers}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.util.{Date}
import javax.swing._
import javax.swing.table.{AbstractTableModel}
import org.apache.log4j.Logger
import state.{PreferencesFactory, GlobalPrefs, PrefKeys}
import twitter.{TwitterMessage, TwitterStatus}
import ui.table.{EmphasizedString, StatusCell}
import util.DesktopUtil

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(val options: StatusTableOptions, val tweetsProvider: BaseProvider, 
    usersModel: UsersModel, filterSet: FilterSet, service: String, 
    username: String, val tagUsers: TagUsers) 
    extends AbstractTableModel with TaggingSupport with Publisher with Reactor {
  
  private val log = Logger.getLogger("StatusTableModel " + tweetsProvider.providerName)
  log.info("Created")

  private val userPrefs = PreferencesFactory.prefsForUser(service, username)

  /** All loaded statuses */
  private var statuses = List[TwitterStatus]()
  
  /** Statuses, after filtering */
  private var filteredStatuses_ = List[TwitterStatus]()
  def filteredStatuses = filteredStatuses_
  
  private val colNames = List("Age", "Image", "From", "To", "Status")
  var preChangeListener: PreChangeListener = _;
  
  listenTo(filterSet)
  reactions += { case FilterSetChanged(s) => filterAndNotify }

  tweetsProvider.addPropertyChangeListener(new PropertyChangeListener {
    def propertyChange(evt: PropertyChangeEvent) = evt.getPropertyName match {
      case TweetsProvider.CLEAR_EVENT => clear(true)
      case TweetsProvider.NEW_TWEETS_EVENT => {
        val listAny = evt.getNewValue.asInstanceOf[List[AnyRef]]
        log.info("Tweets Arrived: " + listAny.length)
        val newTweets = if (listAny == Nil || listAny(0).isInstanceOf[TwitterStatus])
          evt.getNewValue.asInstanceOf[List[TwitterStatus]]
        else
          adaptDmsToTweets(evt.getNewValue.asInstanceOf[List[TwitterMessage]])
        processStatuses(newTweets)
        if (GlobalPrefs.prefs.getBoolean(PrefKeys.NOTIFY_TWEETS, true)) doNotify(newTweets)
      }
    }
  })
  
  var followerIds = List[Long]()
  var friendIds = List[Long]()
  var friendUsernames = List[String]()
  
  def getColumnCount = 5
  def getRowCount = filteredStatuses_.length
  override def getColumnName(column: Int) = colNames(column)

  private val pictureCell = new PictureCell(this, 0)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = filteredStatuses_(rowIndex)
    
    def senderName(status: TwitterStatus) = 
      if (GlobalPrefs.prefs.getBoolean(PrefKeys.USE_REAL_NAMES, true)) 
        UserProperties.overriddenUserName(userPrefs, status.user) 
      else status.user.screenName

    def senderNameEs(status: TwitterStatus): EmphasizedString = 
      new EmphasizedString(Some(senderName(status)), followerIds.contains(status.user.id))

    def toName(status: TwitterStatus) = LinkExtractor.getReplyToUser(getStatusText(status, username)) match {
      case Some(u) => {
         if (GlobalPrefs.prefs.getBoolean(PrefKeys.USE_REAL_NAMES, true)) {
           Some(usersModel.screenNameToUserNameMap.getOrElse(u, u))
         } else {
           Some(u)
         }
      }
      case None => None 
    }
    
    columnIndex match {
      case 0 => status.createdAt.toDate
      case 1 => pictureCell.request(status.user.profileImageURL, rowIndex)
      case 2 => senderNameEs(status)
      case 3 => new EmphasizedString(toName(status), false)
      case 4 => 
        var st = getStatusText(status, username)
        if (options.showToColumn) st = LinkExtractor.getWithoutUser(st)
        StatusCell(if (options.showAgeColumn) None else Some(status.createdAt.toDate),
          if (showNameInStatus) Some(senderNameEs(status)) else None, st)
    }
  }
  
  protected def showNameInStatus = ! options.showNameColumn
  
  def getStatusText(status: TwitterStatus, username: String): String = status.text

  def getStatusAt(rowIndex: Int): TwitterStatus = filteredStatuses_(rowIndex)
  
  override def getColumnClass(col: Int) = List(
    classOf[java.util.Date], 
    classOf[Icon], 
    classOf[String],
    classOf[String], 
    classOf[StatusCell])(col) 
  
  def getIndexOfStatus(statusId: Long): Option[Int] = 
    filteredStatuses_.zipWithIndex.find(si => si._1.id == statusId) match {
      case Some((_, i)) => Some(i)
      case None => None
    }

  def muteSelectedUsers(rows: List[Int]) = muteUsers(getUsers(rows))

  private def muteUsers(users: List[User]) {
    filterSet.mutedUsers ++= users.map(user => (user.id, user))
    filterAndNotify
  }

  def unmuteUsers(userIds: List[Long]) {
    filterSet.mutedUsers --= userIds
    filterAndNotify
  }
  
  def unMuteAll {
    filterSet.mutedUsers.clear
    filterAndNotify
  }

  def muteSelectedUsersRetweets(rows: List[Int]) = muteRetweetUsers(getUsers(rows))

  private def muteRetweetUsers(users: List[User]) {
    filterSet.retweetMutedUsers ++= users.map(user => (user.id, user))
    filterAndNotify
  }

  def unmuteRetweetUsers(userIds: List[Long]) {
    filterSet.retweetMutedUsers --= userIds
    filterAndNotify
  }
  
  def unMuteRetweetAll {
    filterSet.retweetMutedUsers.clear
    filterAndNotify
  }

  def getUsers(rows: List[Int]) = rows.map(i => {
    val user = filteredStatuses_(i).user
    new User(user.id, user.name)
  })
  
  def getStatuses(rows: List[Int]): List[TwitterStatus] = rows.map(filteredStatuses_)

  private def processStatuses(newStatuses: List[TwitterStatus]) {
    statuses :::= newStatuses.reverse
    filterAndNotify
  }
  
  /**
   * Clear (remove) statuses
   */
  def clear(all: Boolean) {
    statuses = if (all) List[TwitterStatus]() else statuses.filter(! filteredStatuses_.contains(_))
    filterAndNotify
  }
  
  def removeStatuses(indexes: List[Int]) {
    val deleteStatuses = getStatuses(indexes)
    statuses = statuses.filter(! deleteStatuses.contains(_))
    filterAndNotify
  }
  
  def removeStatusesFrom(screenNames: List[String]) {
    statuses = statuses.filter(s => ! screenNames.contains(s.user.screenName))
    filterAndNotify
  }

  private def filterAndNotify {
    if (preChangeListener != null) {
      preChangeListener.tableChanging
    }
    
    filteredStatuses_ = filterSet.filter(statuses, friendUsernames, followerIds) 
    publish(new TableContentsChanged(this, filteredStatuses_.length, statuses.length))
    fireTableDataChanged
  }
  
  private def adaptDmsToTweets(dms: List[TwitterMessage]): List[TwitterStatus] = {
    dms.map(dm => new TwitterStatus {
      text = dm.text
      user = if (dm.sender.screenName == username) dm.recipient else dm.sender
      id = dm.id
      createdAt = dm.createdAt
    })
  }

  def doNotify(newTweets: List[TwitterStatus]) = newTweets.length match {
    case 1 => DesktopUtil.notify(newTweets.first.user.screenName+": "+newTweets.first.text,"New tweet")
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

case class TableContentsChanged(val model: StatusTableModel, val filteredIn: Int, 
    val total: Int) extends Event
  
trait Mentions extends StatusTableModel {
  override def getStatusText(status: TwitterStatus, username: String): String = {
    val text = status.text
    val userTag = "@" + username
    if (text.startsWith(userTag)) text.substring(userTag.length).trim else text
  }
}

trait DmsSent extends StatusTableModel {
  override def getValueAt(rowIndex: Int, columnIndex: Int) = super.getValueAt(rowIndex, 
    List(0,1,2,2,4)(columnIndex))
  override def showNameInStatus = false
}

