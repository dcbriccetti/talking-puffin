package com.davebsoft.sctw.ui

import _root_.scala.xml.{NodeSeq, Node}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.{Font}
import javax.swing.table.{DefaultTableCellRenderer, AbstractTableModel}
import javax.swing.{JTable}
import scala.swing._
import scala.swing.GridBagPanel._
import twitter.{FriendsFollowersDataProvider}

/**
 * Displays a list of friends or followers
 */
class FriendsFollowersPane(friends: List[Node], followers: List[Node]) extends GridBagPanel {
  val model = new UsersModel(friends, followers)
  val tableScrollPane = new ScrollPane {
    val table = new JTable(model) {
      setAutoCreateRowSorter(true)
      getColumnModel.getColumn(0).setPreferredWidth(20)
      getColumnModel.getColumn(0).setMaxWidth(30)
      getColumnModel.getColumn(1).setCellRenderer(new AnnotatedUserRenderer)
      getColumnModel.getColumn(4).setPreferredWidth(500)
    }
    peer.setViewportView(table)
  }
  add(new Label("Following: " + friends.size + ", Followers: " + followers.size +
    ", Overlap: " + (friends.size + followers.size - model.combined.size)),
    new Constraints { grid=(0,0); anchor=Anchor.West })
  
  add(tableScrollPane, new Constraints { 
    grid=(0,1); anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1 
  })
}

class UsersModel(friends: List[Node], followers: List[Node]) extends AbstractTableModel {
  private val colNames = List[String](" ", "Screen Name", "Name", "Location", "Description")
  private val elementNames = List[String]("", "screen_name", "name", "location", "description")
  private val set = scala.collection.mutable.Set[Node]()
  set ++ friends
  set ++ followers
  val combined = set.toList.sort((a,b) => 
    ((a \ "name").text.toLowerCase compareTo (b \ "name").text.toLowerCase) < 0)
  
  def getColumnCount = 5
  def getRowCount = combined.length
  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = combined(rowIndex)
    def colVal = (user \ elementNames(columnIndex)).text
    columnIndex match {
      case 0 => {
        val friend = friends.contains(user)
        val follower = followers.contains(user)
        if (friend && follower) "↔" else if (friend) "→" else "←"
      }
      case 1 => new AnnotatedUser(colVal, followers.contains(user))
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
