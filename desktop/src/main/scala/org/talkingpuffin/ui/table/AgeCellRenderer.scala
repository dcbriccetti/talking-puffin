package org.talkingpuffin.ui

import java.awt.{Component}
import java.util.Date
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable}
import time.TimeFormatter

/**
 * Renderer for the Age column
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
        AgeCellRenderer.formatAge(value.asInstanceOf[Date]) + "</font>"))
    this
  }
}

object AgeCellRenderer {
  def formatAge(date: Date) = TimeFormatter(dateToAgeSeconds(date.getTime)).colonSeparated 
  private def dateToAgeSeconds(date: Long): Long = (new Date().getTime() - date) / 1000
}

