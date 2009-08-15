package org.talkingpuffin.ui.util

import java.awt.event.KeyEvent
import javax.swing.{JDialog, JComponent, KeyStroke}
import swing.Action

/**
 * Make a JDialog cancelable with the Escape key
 */
trait Cancelable {
  val dlg = this.asInstanceOf[JDialog] 
  val cancelAction = Action("Cancel") {dlg.setVisible(false)}
  val rp = dlg.getRootPane
  rp.getActionMap.put(cancelAction.title, cancelAction.peer)
  rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelAction.title)
}
 