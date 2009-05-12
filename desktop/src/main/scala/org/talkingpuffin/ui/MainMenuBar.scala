package org.talkingpuffin.ui

import _root_.scala.swing.event.ButtonClicked
import _root_.scala.swing.{MenuItem, MenuBar, Menu, CheckMenuItem, Action}
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.Toolkit

/**
 * Main menu bar
 * 
 * @author Dave Briccetti
 */

class MainMenuBar extends MenuBar {
  contents += new Menu("Session") {
    contents += new MenuItem(new Action("New...") {
      accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, 
        Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
      def apply = Main.launchSession 
    })
  }
  contents += new Menu("Options") {
    object ItemFactory {
      def apply(title: String, tooltip: String, checked: Boolean, 
          mutator: (Boolean) => Unit): MenuItem = {
        val item = new CheckMenuItem(title) {this.tooltip = tooltip; selected = checked}
        listenTo(item)
        reactions += {
          case r: ButtonClicked => if (r.source == item) mutator(item.selected)
          case _ =>
        }
        item
      }
    }
    contents += ItemFactory("Use animations", "Enables simple, useful animations", 
      Globals.options.useAnimations, Globals.options.useAnimations_=_)
    contents += ItemFactory("Look up locations", "Enables lookup of locations from latitude and longitude", 
      Globals.options.lookUpLocations, Globals.options.lookUpLocations_=_)
    contents += ItemFactory("Expand URLs", "Enables fetching original URL from shortened form", 
      Globals.options.expandUrls, Globals.options.expandUrls_=_)
  }
  
}
