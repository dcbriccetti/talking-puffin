package org.talkingpuffin.ui

case class UserSelection(val includeFriends: Boolean, val includeFollowers: Boolean, 
  val searchString: Option[String])
