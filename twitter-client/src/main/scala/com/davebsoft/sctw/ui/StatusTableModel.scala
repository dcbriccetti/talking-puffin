package com.davebsoft.sctw.ui

import _root_.scala.swing.Reactor
import _root_.scala.xml.{NodeSeq, Node}
import filter.{FilterSet, TextFilter, FilterSetChanged, TagUser}
import java.awt.event.{ActionEvent, ActionListener}
import java.util.{Collections, Date, ArrayList}
import javax.swing.event.TableModelEvent
import javax.swing.table.{DefaultTableModel, TableModel, AbstractTableModel}
import javax.swing.{SwingWorker, Timer}
import twitter.{DataFetchException, TweetsProvider}

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(statusDataProvider: TweetsProvider, filterSet: FilterSet, 
    username: String) extends AbstractTableModel with Reactor {
  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  /** All loaded statuses */
  private var statuses = List[Node]()
  
  /** Statuses, after filtering */
  private val filteredStatuses = Collections.synchronizedList(new ArrayList[Node]())
  
  private val colNames = List("Age", "Username", "Status")
  private var timer: Timer = _
  private var preChangeListener: PreChangeListener = _;
  
  listenTo(filterSet)
  reactions += {
    case FilterSetChanged(s) => filterAndNotify
  }
  
  def setPreChangeListener(preChangeListener: PreChangeListener) = this.preChangeListener = preChangeListener
  
  def getColumnCount = 3
  def getRowCount = filteredStatuses.size
  override def getColumnName(column: Int) = colNames(column)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = filteredStatuses.get(rowIndex)
    columnIndex match {
      case 0 => java.lang.Long.valueOf(dateToAgeSeconds((status \ "created_at").text))
      case 1 => (status \ "user" \ "screen_name").text
      case 2 => (status \ "text").text 
    }
  }
  
  def getStatusAt(rowIndex: Int): NodeSeq = {
    filteredStatuses.get(rowIndex)
  }

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case 0 => classOf[java.lang.Long]
      case 1 => classOf[String]
      case 2 => classOf[String] 
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

  private def dateToAgeSeconds(date: String): Long = {
    val df = new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")
    (new Date().getTime - df.parse(date).getTime) / 1000
  }
  
  private def getUsers(rows: List[Int]): List[User] = 
    rows.map(i => {
      val node = filteredStatuses.get(i)
      val id = (node \ "user" \ "id").text
      val name = (node \ "user" \ "name").text
      new User(id, name)
    })
  
  private def createLoadTimer {
    timer = new Timer(updateFrequency, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        loadData
      }
    })
    timer.start
  }
  
  private def loadData {
    new SwingWorker[Option[NodeSeq], Object] {
      override def doInBackground: Option[NodeSeq] = {
        try {
          Some(statusDataProvider.loadTwitterStatusData)
        } catch {
          case ex: DataFetchException => {
            println(ex.response)
            return None
          }
        }
      }
      override def done = {
        get match {
          case Some(statuses) => processStatuses(statuses)
          case None => // Ignore
        }
      }
    }.execute
  }
  
  def loadLastSet {
    clear
    new SwingWorker[NodeSeq, Object] {
      def doInBackground = statusDataProvider.loadLastSet
      override def done = processStatuses(get)
    }.execute
  }
  
  private def processStatuses(newStatuses: NodeSeq) {
    for (st <- newStatuses.reverse) {
      statuses = statuses ::: List(st)
    }
    filterAndNotify
  }
  
  private def filterStatuses {
    filteredStatuses.clear
    for (st <- statuses) {
      var id = (st \ "user" \ "id").text
      if (! filterSet.mutedUsers.contains(id)) {
        if (tagFiltersInclude(id)) {
          val text = (st \ "text").text 
          if (! excludedBecauseReplyAndNotToYou(text)) {
            if (! excludedByStringMatches(text)) {
              filteredStatuses.add(st)
            }
          }
        }
      }
    }
  }
  
  private def tagFiltersInclude(id: String): Boolean = {
    if (filterSet.selectedTags.length == 0) true else {
      for (tag <- filterSet.selectedTags) {
        if (filter.TagUsers.contains(new TagUser(tag, id))) {
          return true
        }
      }
      false
    }
  }
  
  private def excludedBecauseReplyAndNotToYou(text: String): Boolean = {
    val rtu = LinkExtractor.getReplyToUser(text)
    if (! filterSet.excludeNotToYouReplies) return false
    if (rtu.length == 0) return false
    ! rtu.equals(username)
  }

  private def excludedByStringMatches(text: String): Boolean = {
    val includeMatching: TextFilter = if (filterSet.includeTextFilters.size() == 0) null else filterSet.includeTextFilters.get(0) 
    val excludeMatching: TextFilter = if (filterSet.excludeTextFilters.size() == 0) null else filterSet.excludeTextFilters.get(0)
    if (includeMatching == null && excludeMatching == null) return false
    if (includeMatching != null && ! matches(text, includeMatching)) return true
    if (excludeMatching != null && matches(text, excludeMatching)) return true
    false
  }
  
  private def matches(text: String, search: TextFilter): Boolean = {
    if (search.isRegEx) {
      java.util.regex.Pattern.compile(search.text).matcher(text).find
    } else {
      text.contains(search.text)
    }
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
      loadData
    }
  }

  /**
   * Clear (remove) all statuses
   */
  def clear {
    statuses = List[Node]()
    filterAndNotify
  }

  private def filterAndNotify {
    if (preChangeListener != null) {
      preChangeListener.tableChanging
    }
    filterStatuses
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
  
