package com.davebsoft.sctw.ui

import java.awt.event.{ActionListener, ActionEvent}
import scala.swing._
import twitter.{FriendsFollowersDataProvider}

/**
 * Displays a list of friends or followers
 */
class FriendsFollowersPane(dataProvider: FriendsFollowersDataProvider) extends ScrollPane {
  contents = new ListView(dataProvider.getUserNames)
}