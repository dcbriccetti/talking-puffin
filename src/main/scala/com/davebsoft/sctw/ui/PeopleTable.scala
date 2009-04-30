package com.davebsoft.sctw.ui

import javax.swing.table.DefaultTableCellRenderer
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.decorator.HighlighterFactory
import org.jdesktop.swingx.table.TableColumnExt

/**
 * People Table
 * 
 * @author Dave Briccetti
 */

class PeopleTable(tableModel: UsersTableModel) extends JXTable(tableModel) {
  setColumnControlVisible(true)
  setHighlighters(HighlighterFactory.createSimpleStriping)
  setRowHeight(Thumbnail.THUMBNAIL_SIZE + 2)
  setDefaultRenderer(classOf[String], new DefaultTableCellRenderer)
  
  List(UserColumns.NAME, UserColumns.TAGS, UserColumns.LOCATION, UserColumns.STATUS, 
    UserColumns.DESCRIPTION).foreach(i => {
    getColumnModel.getColumn(i).setCellRenderer(new HtmlCellRenderer)
  })
  
  getColumnModel.getColumn(UserColumns.ARROWS).setPreferredWidth(20)
  getColumnModel.getColumn(UserColumns.ARROWS).setMaxWidth(30)
  getColumnModel.getColumn(UserColumns.PICTURE).setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
  val screenNameCol = getColumnModel.getColumn(UserColumns.SCREEN_NAME).asInstanceOf[TableColumnExt]
  screenNameCol.setCellRenderer(new EmphasizedStringCellRenderer)
  screenNameCol.setComparator(EmphasizedStringComparator)
  getColumnModel.getColumn(UserColumns.DESCRIPTION).setPreferredWidth(250)
  getColumnModel.getColumn(UserColumns.STATUS).setPreferredWidth(250)
}
