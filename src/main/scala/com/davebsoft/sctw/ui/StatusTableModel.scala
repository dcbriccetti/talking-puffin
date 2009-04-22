package com.davebsoft.sctw.ui

import _root_.scala.swing.event.Event
import _root_.scala.swing.{Reactor, Publisher}
import _root_.scala.xml.{NodeSeq, Node}
import filter._
import java.awt.event.{ActionEvent, ActionListener}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}

import java.net.URL
import java.util.{Locale, Collections, Date, ArrayList}
import javax.swing._
import javax.swing.event.TableModelEvent
import javax.swing.table.{DefaultTableModel, TableModel, AbstractTableModel}
import org.apache.log4j.Logger
import twitter.{TweetsArrived, DataFetchException, TweetsProvider, Status}

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(val options: StatusTableOptions, tweetsProvider: TweetsProvider, 
    usersModel: UsersTableModel, filterSet: FilterSet, username: String) 
    extends AbstractTableModel with Publisher with Reactor {
  
  private val log = Logger.getLogger("StatusTableModel " + hashCode)
  log.info("Created")

  /** All loaded statuses */
  private var statuses = List[Node]()
  
  def statusCount = statuses.size
  
  /** Statuses, after filtering */
  private val filteredStatuses = Collections.synchronizedList(new ArrayList[Node]())
  
  def filteredStatusCount = filteredStatuses.size

  val filterLogic = new FilterLogic(username, filterSet, filteredStatuses)
  
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
          log.info("Tweets Arrived")
          processStatuses(evt.getNewValue.asInstanceOf[NodeSeq])
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
    columnIndex match {
      case 0 => java.lang.Long.valueOf(dateToAgeSeconds((status \ "created_at").text))
      case 1 => {
        val picUrl = (status \ "user" \ "profile_image_url").text
        pcell.request(picUrl, rowIndex)
      }
      case 2 => {
        val name = (status \ "user" \ "name").text
        val id = (status \ "user" \ "id").text
        new EmphasizedString(Some(name), followerIdsx.contains(id))
      }
      case 3 => {
        val name = (status \ "user" \ "name").text
        val id = (status \ "user" \ "id").text
        val user = LinkExtractor.getReplyToUser(getStatusText(status, username)) match {
          case Some(u) => Some(usersModel.usersModel.screenNameToUserNameMap.getOrElse(u, u))
          case None => None 
        }
        new EmphasizedString(user, false)
      }
      case 4 => {
        val st = getStatusText(status, username)
        if (options.showToColumn) LinkExtractor.getWithoutUser(st) else st 
      }
    }
  }
  
  def getStatusText(status: NodeSeq, username: String): String = (status \ "text").text 

  def getStatusAt(rowIndex: Int): NodeSeq = {
    filteredStatuses.get(rowIndex)
  }

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case 0 => classOf[java.lang.Long]
      case 1 => classOf[Icon]
      case 2 => classOf[String]
      case 3 => classOf[String] 
      case 4 => classOf[String] 
    }
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

  def tagSelectedUsers(rows: List[Int], tag: String) {
    for (user <- getUsers(rows)) {
      filter.TagUsers.add(new TagUser(tag, user.id))
    }
  }

  val df = new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.ENGLISH)
  
  private def dateToAgeSeconds(date: String): Long = {
    (new Date().getTime - df.parse(date).getTime) / 1000
  }
  
  private def getUsers(rows: List[Int]): List[User] = 
    rows.map(i => {
      val node = filteredStatuses.get(i)
      val id = (node \ "user" \ "id").text
      val name = (node \ "user" \ "name").text
      new User(id, name)
    })
  
  def getStatuses(rows: List[Int]): List[Node] = 
    rows.map(filteredStatuses.get(_))

  private def processStatuses(newStatuses: NodeSeq) {
    for (st <- newStatuses.reverse) {
      statuses = statuses ::: List(st)
    }
    filterAndNotify
  }
  
  /**
   * Clear (remove) all statuses
   */
  def clear {
    statuses = List[Node]()
    filterAndNotify
  }
  
  def removeStatuses(indexes: List[Int]) {
    val deleteStatuses = getStatuses(indexes)
    statuses = statuses.filter(! deleteStatuses.contains(_))
    filterAndNotify
  }
  
  def removeStatusesFrom(screenNames: List[String]) {
    statuses = statuses.filter(s => ! screenNames.contains(new Status(s).getScreenNameFromStatus))
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
  
trait Replies extends StatusTableModel {
  override def getStatusText(status: NodeSeq, username: String): String = {
    val text = (status \ "text").text
    val userTag = "@" + username
    if (text.startsWith(userTag)) text.substring(userTag.length).trim else text
  }
}
