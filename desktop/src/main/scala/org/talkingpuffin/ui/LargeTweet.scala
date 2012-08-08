package org.talkingpuffin.ui

import scala.swing.{MenuItem, Action}
import java.awt.{Desktop, Color}
import javax.swing.event.{HyperlinkListener, HyperlinkEvent}
import java.awt.event.{MouseEvent, MouseAdapter}
import javax.swing.{JTextPane, JPopupMenu}
import filter.FiltersDialog
import org.talkingpuffin.Session
import org.talkingpuffin.util.{Loggable, LinkUnIndirector}
import util.{Activateable, eventDistributor, DesktopUtil}

/**
 * A large version of the tweet, which can contain hyperlinks, and from which filters can be created.
 */
class LargeTweet(session: Session, backgroundColor: Color, 
    activateable: Function0[Option[Activateable]]) extends JTextPane with Loggable {
  var filtersDialog: Option[FiltersDialog] = None 
  setBackground(backgroundColor)
  setContentType("text/html")
  setEditable(false)

  private val findLinks =
    if (Desktop.isDesktopSupported)
      Some(LinkUnIndirector.findLinks(DesktopUtil.browse, DesktopUtil.browse) _)
    else
      None

  addHyperlinkListener(new HyperlinkListener() {
    def hyperlinkUpdate(e: HyperlinkEvent) {
      if (e.getEventType == HyperlinkEvent.EventType.ACTIVATED) {
        findLinks.foreach(_(e.getURL.toString))
        activateable().foreach(_.activate) // Let user resume using keyboard to move through tweets
      }
    }
  })
  
  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      showPopup(e)
    }
    override def mouseReleased(e: MouseEvent) {
      showPopup(e)
    }
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
          popup.add(new MenuItem(Action("Create a new view for “" + text + "”") {
            eventDistributor.publish(NewFollowingViewEvent(session, Some(text)))
          }).peer)
          if (popup.getComponentCount > 0)
            popup.show(LargeTweet.this, e.getX, e.getY)
        }
      }
    }
  })
}
