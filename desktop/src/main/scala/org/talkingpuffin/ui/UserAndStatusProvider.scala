package org.talkingpuffin.ui

import javax.swing.table.AbstractTableModel
import twitter4j.{User, Status}

case class UserAndStatus(user: User, retweetedUser: Option[User], status: Option[Status]) {
  def origUser = retweetedUser.getOrElse(user)
}

trait UserAndStatusProvider extends AbstractTableModel {
  def getUserAndStatusAt(rowIndex: Int): UserAndStatus
}