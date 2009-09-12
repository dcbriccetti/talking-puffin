package org.talkingpuffin.util

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

import javax.swing.JPopupMenu
import javax.swing.JTable
import javax.swing.ListSelectionModel

class PopupListener(table: JTable, createPopup: () => JPopupMenu) extends MouseAdapter {

  override def mousePressed(e: MouseEvent) = maybeShowPopup(e)
  override def mouseReleased(e: MouseEvent) = maybeShowPopup(e)

  private def maybeShowPopup(e: MouseEvent) {
    if (e.isPopupTrigger()) {
      val selectionModel = table.getSelectionModel() 
      val mouseRow = table.rowAtPoint(e.getPoint())
      if (! selectionModel.isSelectedIndex(mouseRow)) {
        selectionModel.setSelectionInterval(mouseRow, mouseRow)
      }
      createPopup().show(e.getComponent(), e.getX(), e.getY())
    }
  }
}