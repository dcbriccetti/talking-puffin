package org.talkingpuffin.model

import twitter4j.User

case class FriendsFollowers(friends: List[User], followers: List[User])
