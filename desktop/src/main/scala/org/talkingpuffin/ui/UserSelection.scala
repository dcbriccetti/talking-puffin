package org.talkingpuffin.ui

case class UserSelection(val includeFollowing: Boolean, val includeFollowers: Boolean, 
  val searchString: Option[String])
