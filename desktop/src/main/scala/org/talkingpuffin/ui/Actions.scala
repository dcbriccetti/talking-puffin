package org.talkingpuffin.ui

import _root_.scala.swing.Action
import java.awt.Component
import java.awt.event.KeyEvent
import javax.swing.{KeyStroke, JComponent}

/**
 * Reusable actions with associated accelerators
 */
class EventGeneratingAction(title: String, comp: java.awt.Component, resultKey: Int) extends Action(title) {
  def apply {
    comp.dispatchEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis, 
      0, resultKey, KeyEvent.CHAR_UNDEFINED))
  }
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

class ActionPrep(comp: JComponent) {
  var actions = List[Action]()
  def add(action: Action, keys: KeyStroke*) {
    action.accelerator = Some(keys(0))
    comp.getActionMap.put(action.title, action.peer)
    for (key <- keys) 
      comp.getInputMap.put(key, action.title)
    actions ::= action
  }

  def add(kta: KeyTriggeredAction) {
    add(kta.action, kta.keyStroke: _*)
  }
}
  
