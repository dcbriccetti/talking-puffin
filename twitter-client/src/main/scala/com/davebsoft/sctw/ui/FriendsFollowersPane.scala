package com.davebsoft.sctw.ui

import _root_.scala.xml.Node
import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import scala.swing._
import twitter.{FriendsFollowersDataProvider}

/**
 * Displays a list of friends or followers
 */
class FriendsFollowersPane(dataProvider: FriendsFollowersDataProvider) extends ScrollPane {
  val table = new JTable(new UsersModel(dataProvider.getUsers))
  table.setAutoCreateRowSorter(true)
  table.getColumnModel.getColumn(3).setPreferredWidth(500)
  peer.setViewportView(table)
}

private class UsersModel(users: List[Node]) extends AbstractTableModel {
  private val colNames = List[String]("Screen Name", "Name", "Location", "Description")
  private val elementNames = List[String]("screen_name", "name", "location", "description")
  
  def getColumnCount = 4
  def getRowCount = users.length
  def getValueAt(rowIndex: Int, columnIndex: Int) = (users(rowIndex) \ elementNames(columnIndex)).text
  override def getColumnName(column: Int) = colNames(column)
}