package org.talkingpuffin.ui

import org.talkingpuffin.twitter.{TwitterUser, TwitterStatus}
import javax.swing.table.AbstractTableModel

trait UserAndStatusProvider extends AbstractTableModel {
  def getUserAndStatusAt(rowIndex: Int): Tuple2[TwitterUser, Option[TwitterStatus]]
}