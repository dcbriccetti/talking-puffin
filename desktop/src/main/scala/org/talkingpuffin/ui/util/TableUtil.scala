package org.talkingpuffin.ui.util

import javax.swing.JTable
import javax.swing.table.AbstractTableModel

/**
 * Table utilities.
 */
object TableUtil {
  def getSelectedModelIndexes(table: JTable): List[Int] = {
    val tableRows = table.getSelectedRows
    var smi = List[Int]()
    for (i <- 0 to (tableRows.length - 1)) {
      smi ::= table.convertRowIndexToModel(tableRows(i))
    }
    smi
  }
  
  def invalidateModelIndexes(table: JTable, indexes: List[Int]) {
    val model = table.getModel.asInstanceOf[AbstractTableModel]
    indexes foreach {index => model.fireTableRowsUpdated(index, index)}
  }

}