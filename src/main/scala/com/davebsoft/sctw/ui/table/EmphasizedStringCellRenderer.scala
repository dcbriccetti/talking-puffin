package com.davebsoft.sctw.ui

import java.awt.{Component, Insets, Color}
import java.util.Comparator
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextArea, JTextPane, JTable}

/**
 * From cell renderer.
 * @author Dave Briccetti
 */
class HtmlCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  val border = new EmptyBorder(2, 2, 2, 2)
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(border)
    setFormattedText(value)
    this
  }
  
  protected def setFormattedText(value: Any) {
    val fromTo = value.asInstanceOf[String]
    setText(HtmlFormatter.htmlAround(formatValue(fromTo, renderer.getForeground))) 
  }
  
  private def formatValue(string: String, color: Color): String = {
    "<font face='helvetica' color='#" +  
    Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + 
    string + "</font>"
  }
}

