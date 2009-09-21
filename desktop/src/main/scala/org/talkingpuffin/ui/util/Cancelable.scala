package org.talkingpuffin.ui.util

import java.awt.event.KeyEvent
import javax.swing.{JRootPane, JDialog, JComponent, KeyStroke}
import swing.{Frame, Action}

/**
 * Make a JDialog or Frame cancelable with the Escape key
 */
trait Cancelable {
  var cancelAction: Action = null
  var rp: JRootPane = null
  
  if (isInstanceOf[JDialog]) {
    val dlg = this.asInstanceOf[JDialog] 
    cancelAction = Action("Cancel") {dlg.setVisible(false); notifyOfCancel}
    rp = dlg.getRootPane
  }
  if (isInstanceOf[Frame]) {
    val frame = this.asInstanceOf[Frame] 
    cancelAction = Action("Cancel") {frame.visible = false; notifyOfCancel}
    rp = frame.peer.getRootPane
  }

  rp.getActionMap.put(cancelAction.title, cancelAction.peer)
  rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelAction.title)
  
  def notifyOfCancel {}
}
 