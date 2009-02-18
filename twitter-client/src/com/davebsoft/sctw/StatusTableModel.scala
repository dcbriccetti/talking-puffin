package com.davebsoft.sctw


import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import java.util.{ArrayList, Collections}
import scala.xml.Node
import javax.swing.table.{DefaultTableModel, AbstractTableModel}

/**
 * Model providing status data to the JTable
 */
class StatusTableModel(statusDataProvider: StatusDataProvider) extends AbstractTableModel {
  /** How often, in ms, to fetch and load new data */
  private var updateFrequency = 120 * 1000;
  
  private val statuses = Collections.synchronizedList(new ArrayList[Node]())
  private val colNames = List("Name", "Status")
  private var timer: Timer = null
  
  def getStatuses = statuses
  def getColumnCount = 2
  def getRowCount = statuses.size
  override def getColumnName(column: Int) = colNames(column)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = statuses.get(rowIndex)
    val node = if (columnIndex == 0) status \ "user" \ "name" else status \ "text"
    node.text
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
    statusDataProvider.loadTwitterData(statuses)
    fireTableDataChanged
  }

  /**
   * Sets the update frequency, in seconds.
   */
  def setUpdateFrequency(updateFrequency: Int) {
    this.updateFrequency = updateFrequency * 1000
    if (timer != null && timer.isRunning) {
      timer.stop
    }
    createLoadTimer
    loadData
  }
}
  
