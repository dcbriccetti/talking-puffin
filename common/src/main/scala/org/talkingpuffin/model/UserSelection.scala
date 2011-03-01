package org.talkingpuffin.model

case class UserSelection(includeFriends: Boolean, includeFollowers: Boolean, searchString: Option[String])
