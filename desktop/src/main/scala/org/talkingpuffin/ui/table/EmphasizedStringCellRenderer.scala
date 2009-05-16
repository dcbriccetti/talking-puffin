package org.talkingpuffin.ui.table

import java.awt.Color
import java.util.Comparator
import javax.swing.text.JTextComponent

/**
 * Emphasized string renderer.
 */
class EmphasizedStringCellRenderer extends HtmlCellRenderer {
  override def setFormattedText(component: JTextComponent, value: Any) = component.setText(
    HtmlFormatter.htmlAround(formatValue(value.asInstanceOf[EmphasizedString], renderer.getForeground))) 
  
  private def formatValue(string: EmphasizedString, color: Color): String = {
    string.name match {
      case Some(name) => 
        "<font face='helvetica' color='#" +  
        Integer.toHexString(color.getRGB & 0x00ffffff) + "'>" + 
        EmphasizedStringCellRenderer.decorate(name, string.nameEmphasized) + 
        "</font>"
      case None => ""    
    }
  }
  
}

object EmphasizedStringCellRenderer {
  def decorate(text: String, embolden: Boolean): String = {
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