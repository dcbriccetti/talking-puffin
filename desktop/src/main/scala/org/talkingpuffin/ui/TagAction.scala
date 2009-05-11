package org.talkingpuffin.ui

import _root_.scala.swing.{Key, MenuItem, Action, Separator}
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

        menu.add((new Separator).peer)                
        menu.add(new Action("Remove All Tags") {
          toolTip = "Remove all tags from all users whose tweets are selected"
          accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_A + index, 0)) 
          def apply = statusTableModel.untagSelectedUsers(TableUtil.getSelectedModelIndexes(table))
        }.peer)

        val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
        menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
      case None =>
    }
  }
}
