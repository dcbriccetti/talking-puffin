package org.talkingpuffin.ui

import _root_.scala.swing.{MenuItem, MenuBar, Menu, CheckMenuItem, Action}
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.Toolkit
import swing.event.{Event, ButtonClicked}
import org.talkingpuffin.state.{GlobalPrefs, PrefKeys}
import org.talkingpuffin.Main

/**
 * Main menu bar
 */
class MainMenuBar(dataProviders: DataProviders) extends MenuBar {
  val prefs = GlobalPrefs.prefs

  val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
  
  contents += new Menu("File") {
    contents += new MenuItem(new Action("New Session...") {
      accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask))
      def apply = Main.launchSession 
    })
    contents += new MenuItem(new Action("Close Window") {
      accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKeyMask))
      def apply = TopFrames.closeCurrentWindow()
    })
  }
  
  contents += new Menu("Views") {
    dataProviders.providers.foreach(provider => {
      contents += new MenuItem(new Action("New " + provider.providerName) {
        toolTip = "Creates a new " + provider.providerName + " view"
        def apply = MainMenuBar.this.publish(NewViewEvent(provider))
      })
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
          case r: ButtonClicked => if (r.source == item) GlobalPrefs.put(prefKey, item.selected)
          case _ =>
        }
        item
      }
    }
    contents += ItemFactory("Use animations", "Enables simple, useful animations", 
      PrefKeys.USE_ANIMATIONS, false)
    contents += ItemFactory("Use real names", "Display real names", PrefKeys.USE_REAL_NAMES, true)
    contents += ItemFactory("Notify tweets", "Notify when tweets arrive", PrefKeys.NOTIFY_TWEETS, true)
    contents += ItemFactory("Look up locations", "Enables lookup of locations from latitude and longitude",
      PrefKeys.LOOK_UP_LOCATIONS, false)
    contents += ItemFactory("Expand URLs", "Enables fetching original URL from shortened form", 
      PrefKeys.EXPAND_URLS, false)
    contents += ItemFactory("Show tweet date as age", "Shows the tweet date as an age", 
      PrefKeys.SHOW_TWEET_DATE_AS_AGE, false)
  }
  
}

case class NewViewEvent(val provider: DataProvider) extends Event
