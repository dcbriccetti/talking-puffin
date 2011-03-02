package org.talkingpuffin.ui.table

import java.awt.Color
import java.util.{Date, Comparator}
import javax.swing.text.JTextComponent
import org.talkingpuffin.ui.{HtmlFormatter}
import org.talkingpuffin.time.TimeUtil
import org.talkingpuffin.util.{EscapeHtml, ShortUrl}

/**
 * Status cell renderer.
 */
class StatusCellRenderer extends HtmlCellRenderer {
  var textSizePct = 100
  
  override def setFormattedText(component: JTextComponent, value: Any) = component.setText(
    HtmlFormatter.htmlAround(formatValue(value.asInstanceOf[StatusCell], renderer.getForeground)))

  private def formatValue(cell: StatusCell, color: Color): String = 
      "<font style='font-size: " + textSizePct + "%;' face='helvetica' color='#" + 
      Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + 
    (cell.name match {
      case Some(string) => string.name match {
        case Some(name) => EmphasizedStringCellRenderer.decorate(name, string.nameEmphasized) + ": "
        case None => ""
      }
      case None => ""
    }) + EscapeHtml(ShortUrl.substituteShortenedUrlWith(cell.status, "⁕")) + (cell.age match {
      case Some(age) => "<font size='-2'> " + TimeUtil.formatAge(age) +
          (if (TimeUtil.showAsAge_?) " ago" else "") + "</font>"
      case None => ""
    }) + "</font>"
}

case class StatusCell(age: Option[Date], name: Option[EmphasizedString], status: String)

object StatusComparator extends Comparator[StatusCell] {
  def compare(o1: StatusCell, o2: StatusCell) = o1.status.compareToIgnoreCase(o2.status)
}