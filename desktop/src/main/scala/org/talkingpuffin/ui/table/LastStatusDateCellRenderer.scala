package org.talkingpuffin.ui.table

import java.awt.{Component}
import java.util.Date
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable}
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.talkingpuffin.ui.HtmlFormatter

class LastStatusDateCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  val border = new EmptyBorder(4, 2, 2, 2)
  val fmt = DateTimeFormat.shortDate
  
  override def getTableCellRendererComponent(table: JTable, value: Any,
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(border)

    val date = value.asInstanceOf[Date]
    setText(if (date == new Date(0)) "" else
      HtmlFormatter.htmlAround("<font size='-1' face='helvetica' color='#" +  
        Integer.toHexString(renderer.getForeground.getRGB & 0x00ffffff) + "'>" + 
        fmt.print(new DateTime(date)) + "</font>"))
    this
  }
}

