package org.talkingpuffin.ui

import javax.swing.table.AbstractTableModel
import twitter4j.{User, Status}

trait UserAndStatusProvider extends AbstractTableModel {
  def getUserAndStatusAt(rowIndex: Int): Tuple3[User, Option[User], Option[Status]]
}