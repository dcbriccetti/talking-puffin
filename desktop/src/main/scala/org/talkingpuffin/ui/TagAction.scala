package org.talkingpuffin.ui

import javax.swing.{JTable, SwingUtilities}
import swing.Action
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
        taggingSupport.tagSelectedUsers(modelIndexes, tagsDialog.selectedTags) 
      }
    }
    TableUtil.invalidateModelIndexes(table, modelIndexes)
  }
}

