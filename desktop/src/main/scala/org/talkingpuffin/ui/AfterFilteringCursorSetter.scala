package org.talkingpuffin.ui

import org.apache.log4j.Logger
import javax.swing.JTable

/**
 * Keeps a list of candidate next cursor locations, and sets the cursor from
 * those candidates after filtering or another operation changes the statuses
 * visible in the model.
 */
class AfterFilteringCursorSetter(table: JTable) {
  
  private val log = Logger.getLogger("CursorSetter")
  private var cursorCandidates = List[Long]()
  private val model = table.getModel.asInstanceOf[StatusTableModel] 

  def captureTableState {
    cursorCandidates = table.getSelectedRow match {
      case -1 => List[Long]()
      case selectedRow => (for(row <- selectedRow + 1 until table.getRowCount)  
        yield model.getStatusAt(table.convertRowIndexToModel(row)).id).toList 
    }
    log.debug("Collected " + cursorCandidates.size + " candidates")
  }
  
  def discardCandidates = cursorCandidates = List[Long]()
  
  def setCursor {
    cursorCandidates.foreach { id =>
      model.getIndexOfStatus(id) match {
        case Some(i) =>
          val viewIdx = table.convertRowIndexToView(i)
          table.getSelectionModel.addSelectionInterval(viewIdx, viewIdx);
          log.debug("Found cursor index from candidates: " + i)
          return
        case None =>
      }
    }
  }

}
