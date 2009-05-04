package org.talkingpuffin.ui

import java.awt.{Component, Color}
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable}
import time.TimeFormatter
/**
 * Renderer for the Age column
 * @author Dave Briccetti
 */

class AgeCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  val border = new EmptyBorder(4, 2, 2, 2)
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(border)

    setText(HtmlFormatter.htmlAround("<font size='-1' face='helvetica' color='#" +  
        Integer.toHexString(renderer.getForeground.getRGB & 0x00ffffff) + "'>" + 
        TimeFormatter(value.asInstanceOf[Long]).colonSeparated + "</font>"))
    this
  }

}

