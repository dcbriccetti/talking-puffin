package org.talkingpuffin.ui

import _root_.scala.xml.Node
import javax.swing.table.AbstractTableModel
import javax.swing.Icon
import filter.TagUsers
import table.EmphasizedString

/**
 * A model for users (followed and followers)
 */

class UsersModel(friends: List[Node], followers: List[Node]) {
  var users: Array[Node] = _
  var arrows: Array[String] = _
  var screenNameToUserNameMap = Map[String, String]()
  var friendScreenNames: Set[String] = _

  def build(sel: UserSelection) {
    var set = scala.collection.mutable.Set[Node]()
    if (sel.includeFollowing) set ++ selected(friends  , sel.searchString)
    if (sel.includeFollowers) set ++ selected(followers, sel.searchString)
    val combinedList = set.toList.sort((a,b) => 
      ((a \ "name").text.toLowerCase compareTo (b \ "name").text.toLowerCase) < 0)
    users = combinedList.toArray
    arrows = combinedList.map(user => {
      val friend = friends.contains(user)
      val follower = followers.contains(user)
      if (friend && follower) "↔" else if (friend) "→" else "←"
    }).toArray
    screenNameToUserNameMap = 
        Map(users map {user => ((user \ "screen_name").text, (user \ "name").text)} : _*) 
    friendScreenNames = Set(friends map {f => (f \ "screen_name").text} : _*)    
  }
  
  def selected(users: List[Node], search: Option[String]) = search match {
    case None => users
    case Some(search) => users.filter(u => (u \ "name").text.toLowerCase.contains(search.toLowerCase))
  }
}

case class UserSelection(val includeFollowing: Boolean, val includeFollowers: Boolean, 
  val searchString: Option[String])

class UsersTableModel(tagUsers: TagUsers, var friends: List[Node], var followers: List[Node]) 
    extends AbstractTableModel {
  private val colNames = List(" ", "Image", "Screen Name", "Name", "Tags", "Location", "Description", "Status")
  private val elementNames = List("", "", "screen_name", "name", "", "location", "description", "")
  var usersModel = new UsersModel(friends, followers)
  var lastIncludeFollowing = true
  var lastIncludeFollowers = true
  var lastSearch: Option[String] = None
  buildModelData(UserSelection(true, true, None))

  def buildModelData(sel: UserSelection) {
    lastIncludeFollowing = sel.includeFollowing
    lastIncludeFollowers = sel.includeFollowers
    lastSearch = sel.searchString
    usersModel.build(sel)
    fireTableDataChanged
  }
  
  def usersChanged = {
    usersModel = new UsersModel(friends, followers)
    buildModelData(UserSelection(lastIncludeFollowing, lastIncludeFollowers, lastSearch))
  }
  
  def getColumnCount = 8
  def getRowCount = usersModel.users.length

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case UserColumns.PICTURE => classOf[Icon]
      case _ => classOf[String] 
    }
  }

  val pcell = new PictureCell(this, 1)

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = usersModel.users(rowIndex)
    def colVal = (user \ elementNames(columnIndex)).text
    columnIndex match {
      case UserColumns.PICTURE => {
        val picUrl = (user \ "profile_image_url").text
        pcell.request(picUrl, rowIndex)
      }
      case UserColumns.ARROWS => usersModel.arrows(rowIndex)
      case UserColumns.SCREEN_NAME => new EmphasizedString(Some(colVal), followers.contains(user))
      case UserColumns.TAGS => tagUsers.tagsForUser((user \ "id").text).mkString(", ")
      case UserColumns.STATUS => (user \ "status" \ "text").text
      case _ => colVal
    }
  }
  override def getColumnName(column: Int) = colNames(column)
  
  def getRowAt(rowIndex: Int) = usersModel.users(rowIndex)
}

