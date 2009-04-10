package com.davebsoft.sctw.ui.web

import twitter.{FriendsDataProvider, FollowersDataProvider}

/**
 * Users managed bean.
 * 
 * @author Dave Briccetti
 */

class UserRow(val picUrl: String, val arrows: String, val screenName: String, val name: String, 
    val location: String, val description: String, val status: String) {
  def getPicUrl = picUrl
  def getArrows = arrows
  def getScreenName = screenName
  def getName = name
  def getLocation = location
  def getDescription = description
  def getStatus = status
}

class Users {
  var login: Login = _
  def setLogin(login: Login) = this.login = login
  
  def getUsers: Array[UserRow] = {
    val following = new FriendsDataProvider(login.user, login.password).getUsers
    val followers = new FollowersDataProvider(login.user, login.password).getUsers
    val usersModel = new UsersTableModel(following, followers)
    var users = List[UserRow]()
    val numRows = usersModel.getRowCount
    for (i <- 0 until numRows) {
      val user = usersModel.getRowAt(i)
      users = new UserRow((user \ "profile_image_url").text,
        usersModel.getValueAt(i, UserColumns.ARROWS).asInstanceOf[String], 
        (user \ "screen_name").text, 
        (user \ "name").text, 
        (user \ "location").text, 
        (user \ "description").text, 
        (user \ "status" \ "text").text) :: users
    }
    users.reverse.toArray
  }
}