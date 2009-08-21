package org.talkingpuffin.ui

import _root_.scala.swing.event.Event
import _root_.scala.swing.{Reactor, Publisher}
import filter.{FilterSet, FilterLogic, FilterSetChanged, TagUsers}
import java.awt.event.{ActionEvent, ActionListener}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}

import java.net.URL
import java.util.{Locale, Collections, Date, ArrayList}
import javax.swing._
import javax.swing.event.TableModelEvent
import javax.swing.table.{DefaultTableModel, TableModel, AbstractTableModel}
import org.apache.log4j.Logger
import _root_.org.talkingpuffin
import state.{PreferencesFactory, GlobalPrefs, PrefKeys}
import twitter.{TweetsArrived, TweetsProvider, TwitterStatus}
import ui.table.{EmphasizedString, StatusCell}

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(val options: StatusTableOptions, val tweetsProvider: TweetsProvider, 
    usersModel: UsersTableModel, filterSet: FilterSet, username: String, val tagUsers: TagUsers) 
    extends AbstractTableModel with TaggingSupport with Publisher with Reactor {
  
  private val log = Logger.getLogger("StatusTableModel " + hashCode)
  log.info("Created")

  private val userPrefs = PreferencesFactory.prefsForUser(username)

  /** All loaded statuses */
  private var statuses = List[TwitterStatus]()
  
  def statusCount = statuses.size
  
  /** Statuses, after filtering */
  private val filteredStatuses = Collections.synchronizedList(new ArrayList[TwitterStatus]())
  
  def filteredStatusCount = filteredStatuses.size

  val filterLogic = new FilterLogic(username, tagUsers, filterSet, filteredStatuses)
  
  private val colNames = List("Age", "Image", "From", "To", "Status")
  private var preChangeListener: PreChangeListener = _;
  
  listenTo(filterSet)
  reactions += {
    case FilterSetChanged(s) => filterAndNotify
  }

  tweetsProvider.addPropertyChangeListener(new PropertyChangeListener {
    def propertyChange(evt: PropertyChangeEvent) = {
      evt.getPropertyName match {
        case TweetsProvider.CLEAR_EVENT => clear
        case TweetsProvider.NEW_TWEETS_EVENT => {
          val newTweets = evt.getNewValue.asInstanceOf[List[TwitterStatus]]
          log.info("Tweets Arrived: " + newTweets.length)
          newTweets.foreach(tweet => log.debug("received tweet " + tweet.id))
          processStatuses(newTweets)
        }
      }
    }
  })
  
  private var followerIdsx = List[String]()
  def followerIds = followerIdsx
  def followerIds_=(followerIds: List[String]) = {
    followerIdsx = followerIds
  }
  
  def setPreChangeListener(preChangeListener: PreChangeListener) = this.preChangeListener = preChangeListener
  
  def getColumnCount = 5
  def getRowCount = filteredStatuses.size
  override def getColumnName(column: Int) = colNames(column)

  val pcell = new PictureCell(this, 0)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = filteredStatuses.get(rowIndex)
    
    def age(status: TwitterStatus):java.lang.Long = dateToAgeSeconds(status.createdAt.toDate().getTime())

    def senderName(status: TwitterStatus) = {
      if (GlobalPrefs.prefs.getBoolean(PrefKeys.USE_REAL_NAMES, true)) {
        if (UserProperties.exists(userPrefs, status.user.screenName)) {
          val props = new UserProperties(userPrefs, status.user.screenName)
          props.getName(status.user.name)
        } else status.user.name
        } else {
        status.user.screenName
      }
    }

    def senderNameEs(status: TwitterStatus): EmphasizedString = {
      val name = senderName(status)
      val id = status.user.id.toString()
      new EmphasizedString(Some(name), followerIdsx.contains(id))
    }

    def toName(status: TwitterStatus) = LinkExtractor.getReplyToUser(getStatusText(status, username)) match {
      case Some(u) => {
         if (GlobalPrefs.prefs.getBoolean(PrefKeys.USE_REAL_NAMES, true)) {
           Some(usersModel.usersModel.screenNameToUserNameMap.getOrElse(u, u))
         } else {
           Some(u)
         }
      }
      case None => None 
    }
    
    columnIndex match {
      case 0 => age(status)
      case 1 => pcell.request(status.user.profileImageURL, rowIndex)
      case 2 => senderNameEs(status)
      case 3 => {
        val name = status.user.name
        val id = status.user.id
        new EmphasizedString(toName(status), false)
      }
      case 4 => {
        var st = getStatusText(status, username)
        st = if (options.showToColumn) LinkExtractor.getWithoutUser(st) else st
        StatusCell(if (options.showAgeColumn) None else Some(age(status)),
          if (options.showNameColumn) None else Some(senderNameEs(status)), st)
      }
    }
  }
  
  def getStatusText(status: TwitterStatus, username: String): String = status.text

  def getStatusAt(rowIndex: Int): TwitterStatus = {
    filteredStatuses.get(rowIndex)
  }

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case 0 => classOf[java.lang.Long]
      case 1 => classOf[Icon]
      case 2 => classOf[String]
      case 3 => classOf[String] 
      case 4 => classOf[StatusCell] 
    }
  }
  
  def getIndexOfStatus(statusId: Long): Option[Int] = {
    var i = 0
    while (i < filteredStatuses.size) {
      if (filteredStatuses.get(i).id == statusId) return Some(i)
      i += 1
    }
    return None
  }

  def muteSelectedUsers(rows: List[Int]) {
    muteUsers(getUsers(rows))
  }

  private def muteUsers(users: List[User]) {
    filterSet.mutedUsers ++= users.map(user => (user.id, user))
    filterAndNotify
  }

  def unmuteUsers(userIds: List[String]) {
    filterSet.mutedUsers --= userIds
    filterAndNotify
  }
  
  def unMuteAll {
    filterSet.mutedUsers.clear
    filterAndNotify
  }

  private def dateToAgeSeconds(date: Long): Long = {
    (new Date().getTime() - date) / 1000
  }
  
  def getUsers(rows: List[Int]): List[User] = 
    rows.map(i => {
      val node = filteredStatuses.get(i)
      val id = node.user.id.toString
      val name = node.user.name
      new User(id, name)
    })
  
  def getStatuses(rows: List[Int]): List[TwitterStatus] =
    rows.map(filteredStatuses.get(_))

  private def processStatuses(newStatuses: List[TwitterStatus]) {
    for (st <- newStatuses) {
      statuses = st :: statuses
    }
    filterAndNotify
  }
  
  /**
   * Clear (remove) all statuses
   */
  def clear {
    statuses = List[TwitterStatus]()
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
    filterLogic.filter(statuses)
    publish(new TableContentsChanged(this, filteredStatuses.size, statuses.size))
    fireTableDataChanged
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

