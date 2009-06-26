package org.talkingpuffin.ui

import _root_.scala.swing.{Action,MenuItem}
import java.awt.{Component, Point}
import java.awt.event.KeyEvent
import javax.swing.{JTable, KeyStroke, JPopupMenu}
import org.talkingpuffin.twitter.{TwitterStatus}

/**
 * Shows a menu of links, or launches the browser on the link if there is only one. 
 */
abstract class OpenLinksAction(getSelectedStatus: => Option[TwitterStatus], table: JTable,
    browse: (String) => Unit, title: String) extends Action(title) {
  
  def apply {
    getSelectedStatus match {
      case Some(status) =>
        val urls = LinkExtractor.getLinks(status, users, pages)
  
        if (urls.length == 1) {
          browse(urls(0))
        } else if (urls.length > 1) {
          val menu = new JPopupMenu
          var index = 0
    
          for (url <- urls) {
            val a1 = Action(url) {browse(url)}
            a1.accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_1 + index, 0)) 
            index += 1
            menu.add(new MenuItem(a1).peer)
          }
          val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
          menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
        }
      case None =>
    }
  }
  
  def users: Boolean
  def pages: Boolean
}

class OpenPageLinksAction(getSelectedStatus: => Option[TwitterStatus], table: JTable,
    browse: (String) => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,
    "Open Links…") {
  def users = false
  def pages = true
}

class OpenTwitterUserLinksAction(getSelectedStatus: => Option[TwitterStatus], table: JTable,
    browse: (String) => Unit) extends OpenLinksAction(getSelectedStatus, table, browse,  
    "Open User Links…") {
  def users = true
  def pages = false
}
