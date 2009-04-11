package com.davebsoft.sctw.ui.web

import java.io.Serializable
import twitter.{FriendsDataProvider, FollowersDataProvider}

/**
 * Users managed bean.
 * 
 * @author Dave Briccetti
 */

class UserRow(val picUrl: String, val arrows: String, val screenName: String, val name: String, 
    val location: String, val description: String, val status: String) extends Serializable {
  def getPicUrl = picUrl
  def getArrows = arrows
  def getScreenName = screenName
  def getName = name
  def getLocation = location
  def getDescription = description
  def getStatus = status
}

class Users extends Serializable {
  var login: Login = _
  def setLogin(login: Login) = this.login = login
  
  def getUsers: Array[UserRow] = {
    val following = new FriendsDataProvider(login.user, login.password).getUsers
    val followers = new FollowersDataProvider(login.user, login.password).getUsers
    val usersModel = new UsersModel(following, followers)
    usersModel.build(true, true)
    var users = List[UserRow]()
    for (i <- 0 until usersModel.users.length) {
      val user = usersModel.users(i)
      users = new UserRow((user \ "profile_image_url").text,
        usersModel.arrows(i), 
        (user \ "screen_name").text, 
        (user \ "name").text, 
        (user \ "location").text, 
        (user \ "description").text, 
        (user \ "status" \ "text").text) :: users
    }
    users.reverse.toArray
  }
}