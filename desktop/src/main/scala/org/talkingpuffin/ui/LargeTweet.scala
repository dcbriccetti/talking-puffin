package org.talkingpuffin.ui

import _root_.scala.swing.{MenuItem, Action}
import java.awt.{Desktop, Dimension, Color}
import javax.swing.event.{HyperlinkListener, HyperlinkEvent}
import java.awt.event.{MouseEvent, MouseAdapter}
import javax.swing.{JTable, JTextPane, JPopupMenu}

/**
 * A large version of the tweet, that can contain hyperlinks, and from which filters can be created.
 */

class LargeTweet(filtersDialog: FiltersDialog, viewCreator: ViewCreator, table: JTable, 
    backgroundColor: Color) extends JTextPane {
  setBackground(backgroundColor)
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
          val filterIn = new MenuItem(
            Action("Include tweets containing “" + text + "”")
            {filtersDialog.addIncludeMatching(text)})
          val filterOut = new MenuItem(Action("Exclude tweets containing “" + text + "”")
            {filtersDialog.addExcludeMatching(text)})
          val newStream = new MenuItem(Action("Create a new stream for “" + text + "”")
            {viewCreator.createView(viewCreator.providers.followingProvider, Some(text))})
          popup.add(filterIn.peer)
          popup.add(filterOut.peer)
          popup.add(newStream.peer)
          popup.show(LargeTweet.this, e.getX, e.getY)
        }
      }
    }
  })
  
}