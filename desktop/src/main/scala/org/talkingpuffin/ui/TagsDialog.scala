package org.talkingpuffin.ui

import _root_.scala.swing.{Button, FlowPanel, Action, BorderPanel}
import javax.swing.JDialog
import java.awt.event.KeyEvent
import java.awt.Dimension

/**
 * A dialog from which tags can be selected.
 * 
 * @author Dave Briccetti
 */

class TagsDialog(owner: java.awt.Frame) extends JDialog(owner, "Tags", true) {
  var ok = false
  val panel = new BorderPanel {
    val tagsPanel = new TagsPanel(false) 
    add(tagsPanel, BorderPanel.Position.Center)
  
    add(new FlowPanel {
      val applyAction = Action("Cancel") {TagsDialog.this.setVisible(false)}
      contents += new Button(applyAction)
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
