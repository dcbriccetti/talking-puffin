package com.davebsoft.sctw.ui

import java.awt.{Component, Color}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable}
/**
 * Renderer for the Age column
 * @author Dave Briccetti
 */

class AgeCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(renderer.getBorder)

    val time = value.asInstanceOf[Long]
    val days = time / 86400
    val hours = (time / 3600) - (days * 24)
    val mins = (time / 60) - (days * 1440) - (hours * 60)
    val seconds = time % 60
    val sb = new StringBuilder
    if (days > 0)                   sb.append(twoDigitNum(days)) .append(":")
    if (hours > 0 || sb.length > 0) sb.append(twoDigitNum(hours)).append(":")
    if (mins > 0  || sb.length > 0) sb.append(twoDigitNum(mins)) .append(":")
    sb.append(twoDigitNum(seconds))
    setText(HtmlFormatter.htmlAround("<font size='-1' face='helvetica' color='#" +  
        Integer.toHexString(renderer.getForeground.getRGB & 0x00ffffff) + "'>" + 
        sb.toString + "</font>"))
    this
  }

  private def twoDigitNum(num: java.lang.Long): String = String.format("%02d", num)
  
}

