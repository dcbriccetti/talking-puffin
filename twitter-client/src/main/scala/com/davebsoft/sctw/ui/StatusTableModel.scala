package com.davebsoft.sctw.ui

import com.davebsoft.sctw.twitter.Utils
import _root_.scala.xml.{NodeSeq, Node}
import filter.TagUser
import java.awt.event.{ActionEvent, ActionListener}
import java.util.{Collections, Date, ArrayList}
import javax.swing.event.TableModelEvent
import javax.swing.table.{DefaultTableModel, TableModel, AbstractTableModel}
import javax.swing.{SwingWorker, Timer}
import twitter.{DataFetchException, StatusDataProvider}

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(statusDataProvider: StatusDataProvider, username: String) extends AbstractTableModel {
  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private var statuses = List[Node]()
  private val filteredStatuses = Collections.synchronizedList(new ArrayList[Node]())
  private val mutedIds = scala.collection.mutable.Set[String]()
  private[this] var selectedTags = List[String]()
  private[this] var excludeNotToYouReplies: Boolean = _
  private[this] var includeMatching: String = ""
  private[this] var excludeMatching: String = ""
  private[this] var includeIsRegex: Boolean = _
  private[this] var excludeIsRegex: Boolean = _
  
  private val colNames = List("Age", "Username", "Status")
  private var timer: Timer = _
  private var preChangeListener: PreChangeListener = _;
  
  def setPreChangeListener(preChangeListener: PreChangeListener) = this.preChangeListener = preChangeListener
  
  def getColumnCount = 3
  def getRowCount = filteredStatuses.size
  override def getColumnName(column: Int) = colNames(column)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = filteredStatuses.get(rowIndex)
    columnIndex match {
      case 0 => java.lang.Long.valueOf(dateToAgeSeconds((status \ "created_at").text))
      case 1 => status \ "user"
      case 2 => (status \ "text").text 
    }
  }
  
  def getStatusAt(rowIndex: Int): NodeSeq = {
    filteredStatuses.get(rowIndex)
  }

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case 0 => classOf[java.lang.Long]
      case 1 => classOf[NodeSeq]
      case 2 => classOf[String] 
    }
  }

  def muteSelectedUsers(rows: List[Int]) {
    mutedIds ++= getUserIds(rows)
    filterAndNotify
  }
  
  def unMuteAll {
    mutedIds.clear
    filterAndNotify
  }
  
  def tagSelectedUsers(rows: List[Int], tag: String) {
    for (id <- getUserIds(rows)) {
      filter.TagUsers.add(new TagUser(tag, id))
    }
  }

  def selectedTags_=(tags: List[String]) {
    selectedTags = tags;
  }
  
  def excludeNotToYouReplies_=(ex: Boolean) {
    excludeNotToYouReplies = ex
  }
  
  def setIncludeMatching(text: String, regex: Boolean) {
    includeMatching = text
    includeIsRegex = regex
  }
  
  def setExcludeMatching(text: String, regex: Boolean) {
    excludeMatching = text
    excludeIsRegex = regex
  }
  
  private def dateToAgeSeconds(date: String): Long = {
    val df = new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")
    (new Date().getTime - df.parse(date).getTime) / 1000
  }
  
  private def getUserIds(rows: List[Int]): List[String] = 
    rows.map(i => (filteredStatuses.get(i)\ "user" \ "id").text)
  
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
          return Some(statusDataProvider.loadTwitterStatusData)
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
      if (! mutedIds.contains(id)) {
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
    if (selectedTags.length == 0) true else {
      for (tag <- selectedTags) {
        if (filter.TagUsers.contains(new TagUser(tag, id))) {
          return true
        }
      }
      false
    }
  }
  
  private def excludedBecauseReplyAndNotToYou(text: String): Boolean = {
    val rtu = Utils.getReplyToUser(text)
    if (! excludeNotToYouReplies) return false
    if (rtu.length == 0) return false
    ! rtu.equals(username)
  }

  private def excludedByStringMatches(text: String): Boolean = {
    if (includeMatching.length == 0 && excludeMatching.length == 0) return false
    if (includeMatching.length > 0 && ! matches(text, includeMatching, includeIsRegex)) return true
    if (excludeMatching.length > 0 && matches(text, excludeMatching, excludeIsRegex)) return true
    false
  }
  
  private def matches(text: String, search: String, regex: Boolean): Boolean = {
    if (regex) {
      java.util.regex.Pattern.compile(search).matcher(text).find
    } else {
      text.contains(search)
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
  
  def clear {
    statuses = List[Node]()
    filterAndNotify
  }

  def applyFilters = filterAndNotify
  
  private def filterAndNotify {
    if (preChangeListener != null) {
      preChangeListener.tableChanging
    }
    filterStatuses
    fireTableDataChanged
  }
}

trait PreChangeListener {
  def tableChanging
}
  
