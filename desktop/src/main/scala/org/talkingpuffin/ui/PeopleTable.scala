package org.talkingpuffin.ui

import javax.swing.table.DefaultTableCellRenderer
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.decorator.HighlighterFactory
import org.jdesktop.swingx.table.TableColumnExt
import table.{HtmlCellRenderer, LastStatusDateCellRenderer, EmphasizedStringCellRenderer, EmphasizedStringComparator}

/**
 * People Table
 */
class PeopleTable(tableModel: UsersTableModel) extends JXTable(tableModel) {
  setColumnControlVisible(true)
  setHighlighters(HighlighterFactory.createSimpleStriping)
  setRowHeight(Thumbnail.THUMBNAIL_SIZE + 2)
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer)

  def col(index: Int) = getColumnModel.getColumn(index)
  
  List(UserColumns.NAME, UserColumns.FRIENDS, UserColumns.FOLLOWERS, UserColumns.TAGS, 
    UserColumns.LOCATION, UserColumns.STATUS, UserColumns.DESCRIPTION).foreach(i => {
    col(i).setCellRenderer(new HtmlCellRenderer)
  })
  
  col(UserColumns.ARROWS).setPreferredWidth(20)
  col(UserColumns.ARROWS).setMaxWidth(30)
  col(UserColumns.PICTURE).setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
  val screenNameCol = col(UserColumns.SCREEN_NAME).asInstanceOf[TableColumnExt]
  screenNameCol.setCellRenderer(new EmphasizedStringCellRenderer)
  screenNameCol.setComparator(EmphasizedStringComparator)
  col(UserColumns.DESCRIPTION).setPreferredWidth(250)
  col(UserColumns.STATUS).setPreferredWidth(250)
  col(UserColumns.STATUS_DATE).setCellRenderer(new LastStatusDateCellRenderer)
}
