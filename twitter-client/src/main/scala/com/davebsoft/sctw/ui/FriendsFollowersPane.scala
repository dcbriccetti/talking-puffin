package com.davebsoft.sctw.ui

import _root_.scala.xml.{NodeSeq, Node}
import java.awt.event.{ActionListener, ActionEvent}
import java.awt.{Font}
import java.util.Comparator
import javax.swing.table.{DefaultTableCellRenderer, TableRowSorter, AbstractTableModel}
import javax.swing.{JTable, Icon}
import scala.swing._
import scala.swing.GridBagPanel._
import twitter.{FriendsFollowersDataProvider}

/**
 * Displays a list of friends or followers
 */

object UserColumns {
  val ARROWS = 0
  val PICTURE = 1
  val SCREEN_NAME = 2
  val NAME = 3
  val LOCATION = 4
  val DESCRIPTION = 5
}

class FriendsFollowersPane(friends: List[Node], followers: List[Node]) extends GridBagPanel {
  val model = new UsersModel(friends, followers)
  val tableScrollPane = new ScrollPane {
    val table = new JTable(model) {
      setRowHeight(Thumbnail.THUMBNAIL_SIZE + 2)
      val sorter = new TableRowSorter[UsersModel](model)
      sorter.setComparator(UserColumns.SCREEN_NAME, new Comparator[AnnotatedUser] {
        def compare(o1: AnnotatedUser, o2: AnnotatedUser) = o1.name.compareToIgnoreCase(o2.name)
      })
      setRowSorter(sorter)
      setDefaultRenderer(classOf[String], new DefaultTableCellRenderer with ZebraStriping)
      getColumnModel.getColumn(UserColumns.ARROWS).setPreferredWidth(20)
      getColumnModel.getColumn(UserColumns.ARROWS).setMaxWidth(30)
      getColumnModel.getColumn(UserColumns.PICTURE).setMaxWidth(Thumbnail.THUMBNAIL_SIZE)
      getColumnModel.getColumn(UserColumns.SCREEN_NAME).setCellRenderer(new AnnotatedUserRenderer with ZebraStriping)
      getColumnModel.getColumn(UserColumns.DESCRIPTION).setPreferredWidth(500)
      getColumnModel.getColumn(UserColumns.DESCRIPTION).setCellRenderer(new WordWrappingCellRenderer)
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
  private val colNames = List[String](" ", " ", "Screen Name", "Name", "Location", "Description")
  private val elementNames = List[String]("", " ", "screen_name", "name", "location", "description")
  private val set = scala.collection.mutable.Set[Node]()
  set ++ friends
  set ++ followers
  val combined = set.toList.sort((a,b) => 
    ((a \ "name").text.toLowerCase compareTo (b \ "name").text.toLowerCase) < 0)
  
  def getColumnCount = 6
  def getRowCount = combined.length

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case UserColumns.PICTURE => classOf[Icon]
      case _ => classOf[String] 
    }
  }

  val pcell = new PictureCell(this, 1)

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = combined(rowIndex)
    def colVal = (user \ elementNames(columnIndex)).text
    columnIndex match {
      case UserColumns.PICTURE => {
        val picUrl = (user \ "profile_image_url").text
        pcell.request(picUrl, rowIndex)
      }
      case UserColumns.ARROWS => {
        val friend = friends.contains(user)
        val follower = followers.contains(user)
        if (friend && follower) "↔" else if (friend) "→" else "←"
      }
      case UserColumns.SCREEN_NAME => new AnnotatedUser(colVal, followers.contains(user))
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
