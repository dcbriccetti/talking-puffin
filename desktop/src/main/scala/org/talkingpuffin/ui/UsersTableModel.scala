package org.talkingpuffin.ui

import javax.swing.table.AbstractTableModel
import javax.swing.Icon
import swing.Reactor
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.ui.table.EmphasizedString

class UsersTableModel(val tagUsers: TagUsers, val relationships: Relationships)
    extends AbstractTableModel with TaggingSupport with Reactor {
  private val colNames = List(" ", "Image", "Screen Name", "Name", "Tags", "Location", "Description", "Status")
  private val elementNames = List("", "", "screen_name", "name", "", "location", "description", "")
  var usersModel: UsersModel = _
  var lastIncludeFollowing = true
  var lastIncludeFollowers = true
  var lastSearch: Option[String] = None
  buildModelData(UserSelection(true, true, None))
  reactions += {
    case _: UsersChanged => usersChanged
  }
  listenTo(relationships)

  def buildModelData(sel: UserSelection) {
    lastIncludeFollowing = sel.includeFriends
    lastIncludeFollowers = sel.includeFollowers
    lastSearch = sel.searchString
    usersModel = UsersModel(relationships, sel)
    fireTableDataChanged
  }
  
  private def usersChanged {
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
    columnIndex match {
      case UserColumns.ARROWS => usersModel.arrows(rowIndex)
      case UserColumns.DESCRIPTION => user.description
      case UserColumns.LOCATION => user.location
      case UserColumns.NAME => user.name
      case UserColumns.PICTURE => {
        val picUrl = user.profileImageURL
        pcell.request(picUrl, rowIndex)
      }
      case UserColumns.SCREEN_NAME => new EmphasizedString(Some(user.screenName), relationships.followers.contains(user))
      case UserColumns.STATUS => user.status match {
        case Some(status) => status.text
        case None => ""
      }
      case UserColumns.TAGS => tagUsers.tagsForUser(user.id).mkString(", ")
      case _ => null
    }
  }
  override def getColumnName(column: Int) = colNames(column)
  
  def getRowAt(rowIndex: Int) = usersModel.users(rowIndex)

  def getUsers(rows: List[Int]): List[User] = 
    rows.map(rowIndex => {
      val user = usersModel.users(rowIndex)
      new User(user.id, user.name)
    })
}

