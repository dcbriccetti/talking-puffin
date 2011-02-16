package org.talkingpuffin.ui

import javax.swing.JDialog
import swing.{FlowPanel, Action, Button, BorderPanel}
import org.talkingpuffin.filter.TagUsers
import util.Cancelable

/**
 * A dialog from which tags can be selected.
 */
class TagsDialog(owner: java.awt.Frame, tagUsers: TagUsers, checkedValues: List[String]) 
    extends JDialog(owner, "Tags", true) with Cancelable {
  var ok = false
  val panel = new BorderPanel {
    val tagsPanel = new TagsPanel(false, true, tagUsers, checkedValues)
    add(tagsPanel, BorderPanel.Position.Center)

    add(new FlowPanel {
      contents += new Button(cancelAction)
      val okAction = new Action("OK") {
        def apply = {ok = true; TagsDialog.this.setVisible(false)}
      }
      val okButton = new Button(okAction)
      TagsDialog.this.getRootPane.setDefaultButton(okButton.peer)
      contents += okButton 
    }, BorderPanel.Position.South)
  }
  setContentPane(panel.peer)
  pack
  setLocationRelativeTo(owner)
  
  def selectedTags = panel.tagsPanel.selectedTags
}

