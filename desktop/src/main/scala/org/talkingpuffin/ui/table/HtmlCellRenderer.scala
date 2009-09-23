package org.talkingpuffin.ui

import java.awt.{Color}
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.text.JTextComponent
import javax.swing.{JTextPane, JTable}
import talkingpuffin.util.Loggable

/**
 * HTML cell renderer.
 */
class HtmlCellRenderer extends TableCellRenderer with Loggable {
  val renderer = new DefaultTableCellRenderer
  val border = new EmptyBorder(2, 2, 2, 2)
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = new JTextPane {
    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    setContentType("text/html")
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(border)
    if (value == null) 
      error("Row " + row + ", col " + column + " is null") 
    else 
      setFormattedText(this, value)
  }
  
  protected def setFormattedText(component: JTextComponent, value: Any) = component.setText(
    HtmlFormatter.htmlAround(formatValue(value.asInstanceOf[String], renderer.getForeground))) 
  
  private def formatValue(string: String, color: Color): String = "<font face='helvetica' color='#" +  
    Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + string + "</font>"
}

