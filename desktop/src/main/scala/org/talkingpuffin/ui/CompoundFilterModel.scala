package org.talkingpuffin.ui

import javax.swing.table.AbstractTableModel
import swing.Reactor
import org.talkingpuffin.filter.{CompoundFiltersChanged, CompoundFilters}

class CompoundFilterModel(cpdFilters: CompoundFilters) extends AbstractTableModel with Reactor {
  private val colNames = List("From", "R", "To", "R", "Text", "R", "Source", "R", "RT")
  
  listenTo(cpdFilters)
  reactions += {
    case _: CompoundFiltersChanged => fireTableDataChanged
  }
  
  override def getColumnName(column: Int) = colNames(column)
  def getColumnCount = 9
  def getRowCount = cpdFilters.list.length
  override def getColumnClass(columnIndex: Int) = classOf[String] 

  override def getValueAt(rowIndex: Int, columnIndex: Int): Object = {
    val row = cpdFilters.list(rowIndex)
    columnIndex match {
      case 0 => row.from match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 1 => row.from match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 2 => row.to match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 3 => row.to match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 4 => row.text match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 5 => row.text match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 6 => row.source match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 7 => row.source match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 8 => if (row.retweet.getOrElse(false)) "✓" else ""
    }
  }
}