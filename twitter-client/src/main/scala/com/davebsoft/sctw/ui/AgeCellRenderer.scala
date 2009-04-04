package com.davebsoft.sctw.ui

import java.awt.{Component, Color}
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

/**
 * Renderer for the Age column
 * @author Dave Briccetti
 */

class AgeCellRenderer extends DefaultTableCellRenderer {
  override def setValue(value: Any) {
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
    setText(sb.toString)
  }

  private def twoDigitNum(num: java.lang.Long): String = String.format("%02d", num)
  
}

