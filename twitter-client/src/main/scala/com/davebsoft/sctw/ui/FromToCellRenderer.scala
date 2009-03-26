package com.davebsoft.sctw.ui

import java.awt.{Component, Color}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextArea, JTextPane, JTable}
/**
 * Word wrapping renderer.
 * @author Dave Briccetti
 */

class FromToCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(renderer.getBorder)
    val fromTo = value.asInstanceOf[FromTo]
    setText(HtmlFormatter.htmlAround(formatValue(fromTo, renderer.getForeground))) 
    if (! isSelected) setBackground(if (row % 2 == 0) Color.WHITE else ZebraStriping.VERY_LIGHT_GRAY)
    return this
  }
  
  private def formatValue(fromTo: FromTo, color: Color): String = {
    val toValue = fromTo.to match {
      case Some(to) => "<br/>￫" + decorate(to, fromTo.toEmphasized) 
      case None => ""
    }
    "<table><tr><td><font face='helvetica' color='#" + // TODO find better way than <table> to get padding 
        Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + 
        decorate(fromTo.from, fromTo.fromEmphasized) + toValue + "</font></td></tr></table>"
  }
  
  private def decorate(text: String, embolden: Boolean): String = {
    (if (embolden) "<b>" else "") + text + (if (embolden) "</b>" else "")
  }
  
}