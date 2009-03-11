package com.davebsoft.sctw.ui

import _root_.scala.xml.{NodeSeq, Node}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.Font
import javax.swing.table.{DefaultTableCellRenderer, AbstractTableModel}
import javax.swing.{JTable}
import scala.swing._
import twitter.{FriendsFollowersDataProvider}

/**
 * Displays a list of friends or followers
 */
class FriendsFollowersPane(users: List[Node], xref: List[String]) extends ScrollPane {
  val table = new Table {
    model = new UsersModel(users, xref)
    peer.setAutoCreateRowSorter(true)
    val cm = peer.getColumnModel
    cm.getColumn(0).setCellRenderer(new AnnotatedUserRenderer)
    cm.getColumn(3).setPreferredWidth(500)
  }
  viewportView = table
}

private class UsersModel(users: List[Node], xref: List[String]) extends AbstractTableModel {
  private val colNames     = List[String]("Screen Name", "Name", "Location", "Description")
  private val elementNames = List[String]("screen_name", "name", "location", "description")
  
  def getColumnCount = 4
  def getRowCount = users.length
  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = users(rowIndex)
    val colVal = (user \ elementNames(columnIndex)).text
    columnIndex match {
      case 0 => new AnnotatedUser(colVal, xref.contains((user \ "id").text))
      case _ => colVal
    }
  }
  override def getColumnName(column: Int) = colNames(column)
}

private case class AnnotatedUser(val name: String, val annotated: Boolean)

private class AnnotatedUserRenderer extends DefaultTableCellRenderer {
  val normalFont = getFont
  val boldFont = new Font(normalFont.getFontName, Font.BOLD, normalFont.getSize)
  
  override def setValue(value: Any) {
    val user = value.asInstanceOf[AnnotatedUser]
    setFont(if (user.annotated) boldFont else normalFont)
    setText(user.name)
  }
}
