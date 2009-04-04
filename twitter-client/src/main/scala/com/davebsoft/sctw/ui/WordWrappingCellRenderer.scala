package com.davebsoft.sctw.ui

import java.awt.{Component, Color}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextArea, JTable}

/**
 * Word wrapping renderer.
 * @author Dave Briccetti
 */

class WordWrappingCellRenderer extends JTextArea with TableCellRenderer {
  setLineWrap(true)
  setWrapStyleWord(true)
  val renderer = new DefaultTableCellRenderer
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(renderer.getBorder) 
    setText(renderer.getText) 
    setSize(table.getColumnModel.getColumn(2).getWidth, 0)
    return this
  }
  
}