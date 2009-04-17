package com.davebsoft.sctw.ui

import java.awt.{Component, Color}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextArea, JTable}

/**
 * Word wrapping renderer.
 * @author Dave Briccetti
 */

class WordWrappingCellRenderer extends TableCellRenderer {

  private val renderer = new DefaultTableCellRenderer
  
  override def getTableCellRendererComponent(table: JTable, value: Any,
          isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    /*
    Normally, this class would extend JTextArea, but I found that certain text (Hebrew, I believe)
    “breaks” the JTextArea and causes all further rendering to appear in an ugly font (despite
    attempts to reset the font). To work around, I’m creating a new JTextArea for every cell.
     */
    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    new JTextArea { 
      setLineWrap(true)
      setWrapStyleWord(true)
      setForeground(renderer.getForeground) 
      setBackground(renderer.getBackground) 
      setBorder(renderer.getBorder) 
      setText(renderer.getText)
    }
  }
  
}