package org.talkingpuffin.ui

import java.awt.{Component}
import java.util.Date
import javax.swing.border.EmptyBorder
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable}
import org.talkingpuffin.time.TimeFormatter
import org.joda.time.DateTime
import org.talkingpuffin.state.{PrefKeys, GlobalPrefs}
import org.joda.time.format.DateTimeFormat

/**
 * Renderer for the Age column
 */
class AgeCellRenderer extends JTextPane with TableCellRenderer {
  setContentType("text/html")
  val renderer = new DefaultTableCellRenderer
  val border = new EmptyBorder(4, 2, 2, 2)
  
  override def getTableCellRendererComponent(table: JTable, value: Any,
      isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {

    renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) 
    setForeground(renderer.getForeground) 
    setBackground(renderer.getBackground) 
    setBorder(border)

    setText(HtmlFormatter.htmlAround("<font size='-1' face='helvetica' color='#" +  
        Integer.toHexString(renderer.getForeground.getRGB & 0x00ffffff) + "'>" + 
        AgeCellRenderer.formatAge(value.asInstanceOf[Date]) + "</font>"))
    this
  }
}

object AgeCellRenderer {
  val prefs = GlobalPrefs.prefs
  val fmt = DateTimeFormat.forPattern("MM/dd HH:mm:ss")
  val fmtNoDay = DateTimeFormat.forPattern("HH:mm:ss")

  def showAsAge_? = prefs.getBoolean(PrefKeys.SHOW_TWEET_DATE_AS_AGE, false)
  
  def formatAge(date: Date): String = {
    def today(d1: DateTime) = d1.getDayOfYear == new DateTime().getDayOfYear

    val dateTime = new DateTime(date)
    if (showAsAge_?)
      TimeFormatter(dateToAgeSeconds(date.getTime)).colonSeparated 
    else 
      if (today(dateTime)) fmtNoDay.print(dateTime) else fmt.print(dateTime)
  }
  
  private def dateToAgeSeconds(date: Long): Long = (new Date().getTime() - date) / 1000
}

