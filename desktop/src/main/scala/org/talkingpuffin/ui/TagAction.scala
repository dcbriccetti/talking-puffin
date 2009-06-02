package org.talkingpuffin.ui

import _root_.scala.swing.Action
import twitter.TwitterStatus
import filter.TagsRepository
import javax.swing.{JTable, SwingUtilities}
import util.TableUtil

/**
 * Selecting the tags to apply to users.
 *  
 * @author Dave Briccetti
 */
class TagAction(getSelectedStatus: => Option[TwitterStatus], table: JTable, statusTableModel: StatusTableModel)
    extends Action("Tag With…") {
  def apply {
    getSelectedStatus match {
      case Some(status) => 
        val tagsDialog = new TagsDialog(SwingUtilities.getAncestorOfClass(classOf[java.awt.Frame], 
          table).asInstanceOf[java.awt.Frame])
        tagsDialog.setVisible(true)
        if (tagsDialog.ok) {
          statusTableModel.untagSelectedUsers(TableUtil.getSelectedModelIndexes(table))
          for (tag <- tagsDialog.selectedTags) {
            statusTableModel.tagSelectedUsers(TableUtil.getSelectedModelIndexes(table), tag) 
          }
        }
      case None =>
    }
  }
}

