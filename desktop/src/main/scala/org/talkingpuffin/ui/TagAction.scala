package org.talkingpuffin.ui

import _root_.scala.swing.{MenuItem, Action, Key}
import _root_.scala.xml.NodeSeq
import filter.TagsRepository
import java.awt.event.{KeyEvent}
import util.TableUtil
import javax.swing.{JTable, KeyStroke, JPopupMenu}

/**
 * Builds and operates the tag menu.
 *  
 * @author Dave Briccetti
 */
class TagAction(getSelectedStatus: => Option[NodeSeq], table: JTable, statusTableModel: StatusTableModel) 
    extends Action("Tag With…") {
  def apply {
    getSelectedStatus match {
      case Some(status) =>
        val menu = new JPopupMenu
        var index = 0
  
        for (tag <- TagsRepository.get) {
          val tagSmi = new Action(tag) {
            accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_A + index, 0)) 
            def apply = statusTableModel.tagSelectedUsers(TableUtil.getSelectedModelIndexes(table), tag)
          }
          menu.add(new MenuItem(tagSmi).peer)
          index += 1
        }
        val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
        menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
      case None =>
    }
  }
}
