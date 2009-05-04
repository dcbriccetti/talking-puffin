package org.talkingpuffin.ui.table

import java.awt.Color
import java.util.Comparator
import javax.swing.text.JTextComponent
import time.TimeFormatter

/**
 * Status cell renderer.
 * 
 * @author Dave Briccetti
 */
class StatusCellRenderer extends HtmlCellRenderer {
  override def setFormattedText(component: JTextComponent, value: Any) = component.setText(
    HtmlFormatter.htmlAround(formatValue(value.asInstanceOf[StatusCell], renderer.getForeground))) 
  
  private def formatValue(cell: StatusCell, color: Color): String = 
    "<font face='helvetica' color='#" + Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + 
    (cell.name match {
      case Some(string) => string.name match {
        case Some(name) => EmphasizedStringCellRenderer.decorate(name, string.nameEmphasized) + ": "
        case None => ""
      }
      case None => ""
    }) + cell.status + (cell.age match {
      case Some(age) => "<font size='-2'> " + 
              TimeFormatter(age.asInstanceOf[Long]).colonSeparated + " ago</font>"
      case None => ""
    }) + "</font>"
}

case class StatusCell(val age: Option[java.lang.Long], val name: Option[EmphasizedString], val status: String)

object StatusComparator extends Comparator[StatusCell] {
  def compare(o1: StatusCell, o2: StatusCell) = o1.status.compareToIgnoreCase(o2.status)
}