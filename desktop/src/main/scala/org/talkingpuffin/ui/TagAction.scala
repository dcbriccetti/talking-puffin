package org.talkingpuffin.ui

import _root_.scala.swing.Action
import twitter.TwitterStatus
import javax.swing.{JTable, SwingUtilities}
import util.TableUtil

/**
 * Selecting the tags to apply to users.
 */
class TagAction(table: JTable, taggingSupport: TaggingSupport) extends Action("Tag With…") {
  
  def apply {
    val modelIndexes = TableUtil.getSelectedModelIndexes(table)
    
    if (modelIndexes.length > 0) {
      val tagsDialog = new TagsDialog(SwingUtilities.getAncestorOfClass(classOf[java.awt.Frame],
        table).asInstanceOf[java.awt.Frame], taggingSupport.tagUsers,
        taggingSupport.tagsForSelectedUsers(modelIndexes))
      tagsDialog.setVisible(true)
      
      if (tagsDialog.ok) {
        taggingSupport.untagSelectedUsers(modelIndexes)
        
        for (tag <- tagsDialog.selectedTags) {
          taggingSupport.tagSelectedUsers(modelIndexes, tag) 
        }
      }
    }
    TableUtil.invalidateModelIndexes(table, modelIndexes)
  }
}

