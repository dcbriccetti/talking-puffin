package com.davebsoft.sctw.ui

import _root_.scala.xml.{NodeSeq, Node}
import java.awt.event.{ActionEvent, ActionListener}
import java.util.{ArrayList, Collections}
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
  private val colNames = List("Name", "Status")
  private var timer: Timer = null
  
  def getColumnCount = 2
  def getRowCount = filteredStatuses.size
  override def getColumnName(column: Int) = colNames(column)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = filteredStatuses.get(rowIndex)
    val node = if (columnIndex == 0) status \ "user" \ "name" else status \ "text"
    node.text
  }
  
  def muteSelectedUsers(rows: Array[int]) {
    for (i <- rows) {
      val status = filteredStatuses.get(i)
      mutedIds += (status \ "user" \ "id").text
    }
    filterAndNotify
  }
  
  def unMuteAll {
    mutedIds.clear
    filterAndNotify
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
      def doInBackground = statusDataProvider.loadTwitterStatusData()
      override def done = {
        for (st <- get) {
          statuses ::= st
        }
        filterAndNotify
      }
    }.execute
  }
  
  private def filterStatuses {
    filteredStatuses.clear
    for (st <- statuses) {
      if (! mutedIds.contains((st \ "user" \ "id").text)) {
        filteredStatuses.add(st)
      }
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
  
