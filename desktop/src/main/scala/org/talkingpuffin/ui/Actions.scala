package org.talkingpuffin.ui

import java.awt.Component
import scala.swing.{Action}
import javax.swing.{JMenuItem, JPopupMenu, KeyStroke, JComponent}
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import javax.swing.KeyStroke.{getKeyStroke => ks}

/**
 * Reusable actions with associated accelerators
 */
class EventGeneratingAction(title: String, comp: java.awt.Component, resultKey: Int) extends Action(title) {
  def apply = comp.dispatchEvent(new KeyEvent(comp, KEY_PRESSED, System.currentTimeMillis, 
      0, resultKey, CHAR_UNDEFINED))
}
    
class NextAction(comp: java.awt.Component) extends EventGeneratingAction("Next"    , comp, VK_DOWN)
class PrevAction(comp: java.awt.Component) extends EventGeneratingAction("Previous", comp, VK_UP)

class KeyTriggeredAction(val action: Action, val keyStroke: KeyStroke*)

class NextTAction(comp: Component) extends KeyTriggeredAction(new NextAction(comp), 
  ks(VK_N, 0), KeyStroke.getKeyStroke(VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK))
class PrevTAction(comp: Component) extends KeyTriggeredAction(new PrevAction(comp), 
  ks(VK_P, 0), KeyStroke.getKeyStroke(VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK))

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
  
