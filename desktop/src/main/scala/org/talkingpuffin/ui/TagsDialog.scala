package org.talkingpuffin.ui

import _root_.scala.swing.{Button, FlowPanel, Action, BorderPanel}
import filter.TagUsers
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.{JDialog}
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
        mnemonic = KeyEvent.VK_O
        def apply = {ok = true; TagsDialog.this.setVisible(false)}
      }
      val okButton = new Button(okAction)
      TagsDialog.this.getRootPane.setDefaultButton(okButton.peer)
      contents += okButton 
    }, BorderPanel.Position.South)
  }
  setContentPane(panel.peer)
  setPreferredSize(new Dimension(200,300))
  pack
  setLocationRelativeTo(owner)
  
  def selectedTags = panel.tagsPanel.selectedTags
}

