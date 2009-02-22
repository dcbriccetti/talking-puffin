package com.davebsoft.sctw.ui

import _root_.scala.xml.{NodeSeq, Node}
import filter.TagUser
import java.awt.event.{ActionEvent, ActionListener}
import java.util.{Collections, Date, ArrayList}
import javax.swing.{SwingWorker, Timer}
import javax.swing.table.{DefaultTableModel, AbstractTableModel}
import twitter.StatusDataProvider

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(statusDataProvider: StatusDataProvider) extends AbstractTableModel {
  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private var statuses = List[Node]()
  private val filteredStatuses = Collections.synchronizedList(new ArrayList[Node]())
  private val mutedIds = scala.collection.mutable.Set[String]()
  private var selectedTags = List[String]()
  private val colNames = List("Age", "Name", "Status")
  private var timer: Timer = null
  
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

  def setSelectedTags(selectedTags: List[String]) {
    this.selectedTags = selectedTags;
    filterAndNotify
  }
  
  private def dateToAgeSeconds(date: String): Long = {
    val df = new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")
    (new Date().getTime - df.parse(date).getTime) / 1000
  }
  
  private def getUserIds(rows: List[Int]): List[String] = {
    var ids = List[String]()
    for (i <- rows) {
      val status = filteredStatuses.get(i)
      ids ::= (status \ "user" \ "id").text
    }
    ids
  }
  
  private def createLoadTimer {
    timer = new Timer(updateFrequency, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        loadData
      }
    })
    timer.start
  }
  
  private def loadData {
    new SwingWorker[NodeSeq, Object] {
      def doInBackground = statusDataProvider.loadTwitterStatusData
      override def done = processStatuses(get)
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
          filteredStatuses.add(st)
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

  private def filterAndNotify {
    filterStatuses
    fireTableDataChanged
  }
}
  
