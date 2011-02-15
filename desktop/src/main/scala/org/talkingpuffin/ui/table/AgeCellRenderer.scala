package org.talkingpuffin.ui.table

import java.awt.{Component}
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable}
import org.talkingpuffin.time.TimeUtil
import java.util.Date

/**
 * Renderer for the Age column
 */
class AgeCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  val border = new EmptyBorder(4, 2, 2, 2)

  override def getTableCellRendererComponent(table: JTable, value: Any,
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {
    setForeground(renderer.getForeground)
    setBackground(renderer.getBackground)
    setBorder(border)
    setText(TimeUtil.asHTML(value.asInstanceOf[Date]))
    this
  }
}

