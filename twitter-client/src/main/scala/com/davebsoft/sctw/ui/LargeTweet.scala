package com.davebsoft.sctw.ui

import _root_.scala.swing.{MenuItem, Action}
import java.awt.{Desktop, Dimension, Color}
import javax.swing.event.{HyperlinkListener, HyperlinkEvent}
import java.awt.event.{MouseEvent, MouseAdapter}
import javax.swing.{JTable, JTextPane, JPopupMenu}

/**
 * A large version of the tweet, that can contain hyperlinks, and from which filters can be created.
 * @author Dave Briccetti
 */

class LargeTweet(filtersPane: FiltersPane, table: JTable, backgroundColor: Color) extends JTextPane {
  val dim = new Dimension(500, 100)
  setBackground(backgroundColor)
  setMinimumSize(dim)
  setPreferredSize(dim)
  setContentType("text/html");
  setEditable(false);
  
  addHyperlinkListener(new HyperlinkListener() {
    def hyperlinkUpdate(e: HyperlinkEvent) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        if (Desktop.isDesktopSupported) {
          Desktop.getDesktop.browse(e.getURL().toURI)
        }
        table.requestFocusInWindow // Let user resume using keyboard to move through tweets
      }
    }
  });
  
  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) = showPopup(e)
    override def mouseReleased(e: MouseEvent) = showPopup(e)
    def showPopup(e: MouseEvent) {
      if (e.isPopupTrigger) {
        val text = getSelectedText
        if (text != null) {
          val popup = new JPopupMenu
          val filterInMenu = new MenuItem(
            Action("Include tweets containing “" + text + "”")
            {filtersPane.addIncludeMatching(text)})
          val filterOutMenu = new MenuItem(Action("Exclude tweets containing “" + text + "”")
            {filtersPane.addExcludeMatching(text)})
          popup.add(filterInMenu.peer)
          popup.add(filterOutMenu.peer)
          popup.show(LargeTweet.this, e.getX, e.getY)
        }
      }
    }
  })
  
}