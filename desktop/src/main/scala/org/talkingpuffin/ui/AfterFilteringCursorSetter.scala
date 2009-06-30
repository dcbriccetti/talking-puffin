package org.talkingpuffin.ui

import apache.log4j.Logger
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
    var candidates = List[Long]()
    val selectedRow = table.getSelectedRow
    if (selectedRow != -1) {
      var i = selectedRow + 1
      val numRows = table.getRowCount
      while(i < numRows) {
        candidates = model.getStatusAt(table.convertRowIndexToModel(i)).id :: candidates
        i += 1
      }
    }
    cursorCandidates = candidates.reverse
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
