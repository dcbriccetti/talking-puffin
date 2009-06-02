package org.talkingpuffin.ui

import _root_.scala.swing.Action
import twitter.TwitterStatus
import filter.TagsRepository
import javax.swing.{JTable, SwingUtilities}
import util.TableUtil

/**
 * Selecting the tags to apply to users.
 */
class TagAction(table: JTable, taggingSupport: TaggingSupport)
    extends Action("Tag With…") {
  def apply {
    val selectedModelIndexes = TableUtil.getSelectedModelIndexes(table)
    if (selectedModelIndexes.length > 0) {
      val tagsDialog = new TagsDialog(SwingUtilities.getAncestorOfClass(classOf[java.awt.Frame], 
        table).asInstanceOf[java.awt.Frame])
      tagsDialog.setVisible(true)
      if (tagsDialog.ok) {
        taggingSupport.untagSelectedUsers(selectedModelIndexes)
        for (tag <- tagsDialog.selectedTags) {
          taggingSupport.tagSelectedUsers(selectedModelIndexes, tag) 
        }
      }
    }
  }
}

