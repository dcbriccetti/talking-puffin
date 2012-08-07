package org.talkingpuffin.model

case class UserSelection(includeFriends: Boolean, includeFollowers: Boolean, includeEmptyDescriptions: Boolean,
                         searchString: Option[String])
