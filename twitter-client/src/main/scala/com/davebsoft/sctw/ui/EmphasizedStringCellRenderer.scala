package com.davebsoft.sctw.ui

import java.awt.{Component, Color}
import java.util.Comparator
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextArea, JTextPane, JTable}
/**
 * From cell renderer.
 * @author Dave Briccetti
 */

class EmphasizedStringCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  
  override def getTableCellRendererComponent(table: JTable, value: Any, 
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(renderer.getBorder)
    val fromTo = value.asInstanceOf[EmphasizedString]
    setText(HtmlFormatter.htmlAround(formatValue(fromTo, renderer.getForeground))) 
    return this
  }
  
  private def formatValue(string: EmphasizedString, color: Color): String = {
    string.name match {
      case Some(name) => 
    "<table><tr><td><font face='helvetica' color='#" + // TODO find better way than <table> to get padding 
        Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + 
        decorate(name, string.nameEmphasized) + "</font></td></tr></table>"
      case None => ""    
    }
  }
  
  private def decorate(text: String, embolden: Boolean): String = {
    (if (embolden) "<b>" else "") + text + (if (embolden) "</b>" else "")
  }
  
}

class EmphasizedString(val name: Option[String], val nameEmphasized: Boolean)

object EmphasizedStringComparator extends Comparator[EmphasizedString] {
  def compare(o1: EmphasizedString, o2: EmphasizedString) = {
    def nameToString(fromTo: EmphasizedString): String = fromTo.name match {
      case Some(name) => name 
      case None => ""
    }
    nameToString(o1).compareToIgnoreCase(nameToString(o2))
  }
  
}