package org.talkingpuffin.ui

import _root_.scala.swing.event.ButtonClicked
import _root_.scala.swing.{MenuItem, MenuBar, Menu, CheckMenuItem, Action}
import apache.log4j.Logger
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.Toolkit
import state.{GlobalPrefs, PrefKeys}

/**
 * Main menu bar
 */
class MainMenuBar extends MenuBar {
  val prefs = GlobalPrefs.prefs
  
  contents += new Menu("Session") {
    contents += new MenuItem(new Action("New...") {
      accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, 
        Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
      def apply = Main.launchSession 
    })
  }
  contents += new Menu("Options") {
    object ItemFactory {
      def apply(title: String, tooltip: String, prefKey: String, onByDefault: Boolean): MenuItem = {
        val item = new CheckMenuItem(title) {
          this.tooltip = tooltip; 
          selected = prefs.getBoolean(prefKey, onByDefault)
        }
        listenTo(item)
        reactions += {
          case r: ButtonClicked => if (r.source == item) {
            prefs.putBoolean(prefKey, item.selected)
          }
          case _ =>
        }
        item
      }
    }
    contents += ItemFactory("Use animations", "Enables simple, useful animations", 
      PrefKeys.USE_ANIMATIONS, false)
    contents += ItemFactory("Use real names", "Display real names", PrefKeys.USE_REAL_NAMES, true)
    contents += ItemFactory("Look up locations", "Enables lookup of locations from latitude and longitude",
      PrefKeys.LOOK_UP_LOCATIONS, false)
    contents += ItemFactory("Expand URLs", "Enables fetching original URL from shortened form", 
      PrefKeys.EXPAND_URLS, false)
  }
  
}
