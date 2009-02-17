package com.davebsoft.sctw

import java.util.{ArrayList, Collections}
import scala.xml.Node
import javax.swing.table.{DefaultTableModel, AbstractTableModel}

/**
 * Model providing status data to the JTable
 */
class StatusTableModel extends AbstractTableModel {
  private val statuses = Collections.synchronizedList(new ArrayList[Node]())
  private val colNames = List("Name", "Status")

  def getStatuses = statuses
  def getColumnCount = 2
  def getRowCount = statuses.size
  override def getColumnName(column: Int) = colNames(column)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val status = statuses.get(rowIndex)
    val node = if (columnIndex == 0) status \ "user" \ "name" else status \ "text"
    node.text
  }
}
  
