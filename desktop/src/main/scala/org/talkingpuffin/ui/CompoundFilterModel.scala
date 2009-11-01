package org.talkingpuffin.ui

import javax.swing.table.AbstractTableModel
import swing.Reactor
import org.talkingpuffin.filter._

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
    val cpdFilter = cpdFilters.list(rowIndex)
    columnIndex match {
      case 0 => cpdFilter.textFilters.find(_.isInstanceOf[FromTextFilter]) match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 1 => cpdFilter.textFilters.find(_.isInstanceOf[FromTextFilter]) match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 2 => cpdFilter.textFilters.find(_.isInstanceOf[ToTextFilter]) match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 3 => cpdFilter.textFilters.find(_.isInstanceOf[ToTextFilter]) match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 4 => cpdFilter.textFilters.find(_.isInstanceOf[TextTextFilter]) match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 5 => cpdFilter.textFilters.find(_.isInstanceOf[TextTextFilter]) match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 6 => cpdFilter.textFilters.find(_.isInstanceOf[SourceTextFilter]) match {
        case Some(cf) => cf.text
        case _ => ""
      }
      case 7 => cpdFilter.textFilters.find(_.isInstanceOf[SourceTextFilter]) match {
        case Some(cf) => if (cf.isRegEx) "✓" else ""
        case _ => ""
      }
      case 8 => if (cpdFilter.retweet.getOrElse(false)) "✓" else ""
    }
  }
}