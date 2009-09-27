package org.talkingpuffin.ui.util

import javax.swing.JToolBar

trait ToolBarHelpers extends JToolBar {
  def aa(actions: scala.swing.Action*) = actions.foreach(action => add(action.peer).setFocusable(false))
  def ac(comps: java.awt.Component*) = comps.foreach(comp => add(comp).setFocusable(false))
}