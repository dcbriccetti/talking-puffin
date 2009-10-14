package org.talkingpuffin.ui

import java.awt.Component
import java.awt.event.KeyEvent
import scala.swing.{Action}
import javax.swing.{JMenuItem, JPopupMenu, KeyStroke, JComponent}

/**
 * Reusable actions with associated accelerators
 */
class EventGeneratingAction(title: String, comp: java.awt.Component, resultKey: Int) extends Action(title) {
  def apply = comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
      0, resultKey, KeyEvent.CHAR_UNDEFINED))
}
    
class NextAction(comp: java.awt.Component) extends EventGeneratingAction("Next"    , comp, KeyEvent.VK_DOWN)
class PrevAction(comp: java.awt.Component) extends EventGeneratingAction("Previous", comp, KeyEvent.VK_UP)

class KeyTriggeredAction(val action: Action, val keyStroke: KeyStroke*)

class NextTAction(comp: Component) extends KeyTriggeredAction(new NextAction(comp), 
  Actions.ks(KeyEvent.VK_N), KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK))
class PrevTAction(comp: Component) extends KeyTriggeredAction(new PrevAction(comp), 
  Actions.ks(KeyEvent.VK_P), KeyStroke.getKeyStroke(KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK))

object Actions {
  def ks(keyEvent: Int): KeyStroke = KeyStroke.getKeyStroke(keyEvent, 0)
}

class PopupMenuHelper(comp: JComponent) {
  val menu = new JPopupMenu
  
  def add(action: Action, keys: KeyStroke*) {
    add(action, menu, keys: _*)
  }
  
  def add(action: Action, menu: JComponent, keys: KeyStroke*) {
    if (keys.length > 0) action.accelerator = Some(keys(0))
    comp.getActionMap.put(action.title, action.peer)
    keys foreach(comp.getInputMap.put(_, action.title))
    menu.add(new JMenuItem(action.peer))
  }

  def add(kta: KeyTriggeredAction): Unit = add(kta.action, kta.keyStroke: _*)
}
  
