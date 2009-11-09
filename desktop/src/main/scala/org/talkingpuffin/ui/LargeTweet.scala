package org.talkingpuffin.ui

import _root_.scala.swing.{MenuItem, Action}
import java.awt.{Desktop, Color}
import javax.swing.event.{HyperlinkListener, HyperlinkEvent}
import java.awt.event.{MouseEvent, MouseAdapter}
import util.{DesktopUtil}
import org.talkingpuffin.util.LinkUnIndirector
import filter.FiltersDialog
import javax.swing.{JComponent, JTextPane, JPopupMenu}

/**
 * A large version of the tweet, that can contain hyperlinks, and from which filters can be created.
 */
class LargeTweet(filtersDialog: Option[FiltersDialog], viewCreator: Option[ViewCreator], 
    focusAfterHyperlinkClick: JComponent, backgroundColor: Color) extends JTextPane {
  setBackground(backgroundColor)
  setContentType("text/html");
  setEditable(false);
  
  addHyperlinkListener(new HyperlinkListener() {
    def hyperlinkUpdate(e: HyperlinkEvent) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        if (Desktop.isDesktopSupported) {
          LinkUnIndirector.findLinks(DesktopUtil.browse, DesktopUtil.browse)(e.getURL.toString)
        }
        focusAfterHyperlinkClick.requestFocusInWindow // Let user resume using keyboard to move through tweets
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
          filtersDialog match {
            case Some(fd) =>
              popup.add(new MenuItem(Action("Include tweets containing “" + text + "”")
                  {fd.addIncludeMatching(text)}).peer)
              popup.add(new MenuItem(Action("Exclude tweets containing “" + text + "”")
                  {fd.addExcludeMatching(text)}).peer)
            case _ =>
          }
          viewCreator match {
            case Some(vc) =>
              popup.add(new MenuItem(Action("Create a new stream for “" + text + "”")
                  {vc.createView(vc.providers.following, Some(text))}).peer)
            case _ =>
          }
          if (popup.getComponentCount > 0)
            popup.show(LargeTweet.this, e.getX, e.getY)
        }
      }
    }
  })
  
}