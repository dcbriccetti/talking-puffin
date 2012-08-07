package org.talkingpuffin.ui

import javax.swing.Icon
import java.util.Date
import swing.Reactor
import twitter4j.User
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.ui.table.EmphasizedString
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.apix.RichUser._
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.model.{UserSelection, UsersModel}

class UsersTableModel(users: Option[List[User]], val tagUsers: TagUsers,
    val relationships: Relationships) 
    extends UserAndStatusProvider with TaggingSupport with Reactor with Loggable {
  
  val pcell = new PictureCell(this, 1)
  private val colNames = List(" ", "Image", "Screen Name", "Name", "Friends", "Follwrs", 
    "Tags", "Location", "Description", "Status", "St Date")
  var usersModel: UsersModel = _
  var lastIncludeFriends = true
  var lastIncludeFollowers = true
  var lastIncludeEmptyDescriptions = true
  var lastSearch: Option[String] = None
  buildModelData(UserSelection(includeFriends = true, includeFollowers = true,
    includeEmptyDescriptions = true, searchString = None))
  reactions += {
    case u: UsersChanged => if (u.source eq relationships) usersChanged
  }
  listenTo(relationships)

  def getColumnCount = UserColumns.Count
  def getRowCount = usersModel.users.length

  override def getColumnClass(columnIndex: Int) = {
    columnIndex match {
      case UserColumns.PICTURE => classOf[Icon]
      case UserColumns.FRIENDS => classOf[Int]
      case UserColumns.FOLLOWERS => classOf[Int]
      case UserColumns.STATUS_DATE => classOf[Date]
      case _ => classOf[String] 
    }
  }

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val user = usersModel.users(rowIndex)
    columnIndex match {
      case UserColumns.ARROWS => usersModel.arrows(rowIndex)
      case UserColumns.DESCRIPTION => user.description
      case UserColumns.LOCATION => user.location
      case UserColumns.NAME => user.getName
      case UserColumns.PICTURE => {
        val picUrl = user.getProfileImageURL.toString
        pcell.request(picUrl, rowIndex)
      }
      case UserColumns.SCREEN_NAME => new EmphasizedString(Some(user.getScreenName), relationships.followers.contains(user))
      case UserColumns.FRIENDS => user.getFriendsCount.asInstanceOf[Object]
      case UserColumns.FOLLOWERS => user.getFollowersCount.asInstanceOf[Object]
      case UserColumns.STATUS => user.status match {
        case Some(status) => status.text
        case None => ""
      }
      case UserColumns.STATUS_DATE => user.status match {
        case Some(status) => status.createdAt.toDate
        case None => new Date(0)
      }
      case UserColumns.TAGS => tagUsers.tagsForUser(user.getId).mkString(", ")
      case _ => null
    }
  }
  override def getColumnName(column: Int) = colNames(column)
  
  def getRowAt(rowIndex: Int) = usersModel.users(rowIndex)
  
  def getUserAndStatusAt(rowIndex: Int): UserAndStatus = {
    val user = getRowAt(rowIndex)
    UserAndStatus(user, None, user.status)
  }

  def getUsers(rows: List[Int]): List[UserIdName] =
    rows.map(rowIndex => {
      val user = usersModel.users(rowIndex)
      new UserIdName(user.getId, user.getName)
    })

  private[ui] def buildModelData(sel: UserSelection) {
    lastIncludeFriends = sel.includeFriends
    lastIncludeFollowers = sel.includeFollowers
    lastIncludeEmptyDescriptions = sel.includeEmptyDescriptions
    lastSearch = sel.searchString
    usersModel = UsersModel(users, relationships, sel)
    fireTableDataChanged()
  }
  
  private def usersChanged() {
    buildModelData(UserSelection(lastIncludeFriends, lastIncludeFollowers, lastIncludeEmptyDescriptions, lastSearch))
  }
  
}

