package org.talkingpuffin.ui

import _root_.scala.swing.{Action,MenuItem}
import java.awt.event.KeyEvent
import javax.swing.{JTable, JPopupMenu}
import javax.swing.KeyStroke.getKeyStroke
import twitter4j.Status
import org.talkingpuffin.apix.RichStatus._
import org.talkingpuffin.util.LinkExtractor

/**
 * Shows a menu of links, or launches the browser on the link if there is only one. 
 */
abstract class OpenLinksAction(getSelectedStatus: => Option[Status], table: JTable,
    browse: String => Unit, title: String) extends Action(title) {
  
  def apply {
    def addMenuItem(menu: JPopupMenu, title: String, accelIndex: Int, action: => Unit) = {
      val a1 = Action(title) {action}
      a1.accelerator = Some(getKeyStroke(KeyEvent.VK_1 + accelIndex, 0))
      menu.add(new MenuItem(a1).peer)
    }
    
    def browseUrls(urls: List[String]) = urls.foreach(browse)

    def stripHttpProtocolString(url: String): String = {
      val prefix = "http://"
      if (url startsWith prefix) url substring prefix.length else url
    }

    getSelectedStatus match {
      case Some(status) =>
        val urls = LinkExtractor.getLinks(status.text, status.inReplyToStatusId, users, links, lists)
  
        if (urls.length == 1) {
          browse(urls(0).link)
        } else if (urls.length > 1) {
          val menu = new JPopupMenu
          var index = 0
    
          for (url <- urls) {
            addMenuItem(menu, stripHttpProtocolString(url.title), index, browse(url.link))
            index += 1
          }
          
          if (urls.length > 1) {
            val a1 = Action(if (urls.length == 2) "Both" else "All") {browseUrls(urls.map(_.link))}
            a1.accelerator = Some(getKeyStroke(KeyEvent.VK_A, 0))
            menu.add(new MenuItem(a1).peer)
          }
          val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
          menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
        }
      case None =>
    }
  }
  
  def users: Boolean
  def links: Boolean
  def lists: Boolean
}

class OpenPageLinksAction(getSelectedStatus: => Option[Status], table: JTable,
    browse: String => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,
    "Open Links…") {
  def users = false
  def links = true
  def lists = false
}

class OpenTwitterUserLinksAction(getSelectedStatus: => Option[Status], table: JTable,
    browse: String => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,
    "Open User Links…") {
  def users = true
  def links = false
  def lists = false
}

class OpenTwitterUserListsAction(getSelectedStatus: => Option[Status], table: JTable,
    browse: String => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,
    "Open User Lists…") {
  def users = false
  def links = false
  def lists = true
}
