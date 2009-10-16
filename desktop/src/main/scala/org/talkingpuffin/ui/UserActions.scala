package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.Toolkit
import swing.{MenuItem, Action}
import util.DesktopUtil
import scala.xml.NodeSeq
import javax.swing._
import org.talkingpuffin.twitter.{AuthenticatedSession}

/**
 * Handles user actions like follow
 */
class UserActions(twitterSession: AuthenticatedSession, rels: Relationships) {
  def follow(names: List[String]) = process(names, twitterSession.createFriendship, "following")
  
  def unfollow(names: List[String]) {
    process(names, twitterSession.destroyFriendship, "unfollowing")
    rels.removeFriendsWithScreenNames(names)
  }

  def block(names: List[String]) {
    process(names, twitterSession.blockUser, "block")
    rels.removeFriendsWithScreenNames(names)
  }
  
  def unblock(names: List[String]) = process(names, twitterSession.unblockUser, "unblock")
  
  def reportSpam(names: List[String]) = process(names, twitterSession.reportSpam, "report spam")
  
  def viewLists(selectedScreenNames: List[String], table: JTable) {
    selectedScreenNames.foreach(screenName => {
      SwingInvoke.execSwingWorker({
        twitterSession.getLists(screenName)
      }, {
        listsNode: NodeSeq => {
          val lists = listsNode \ "list"
          if (lists.length > 0) {
            val menu = new JPopupMenu
            lists.foreach(l => {
              val a1 = Action((l \ "name").text) {DesktopUtil.browse("http://twitter.com/" + (l \ "uri").text)}
              menu.add(new MenuItem(a1).peer)
            })
            val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
            menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
          }
        }
      })
    })    
  }

  private def process(names:List[String], action:((String) => Unit), actionName: String) = 
    names foreach {name => 
      try {
        action(name)
      } catch {
        case e: Throwable => showFollowErr(e, actionName, name)
      }
    }

  private def showFollowErr(e:Throwable,action:String,screenName:String){
    JOptionPane.showMessageDialog(null, "Error " + action + " " + screenName)
  }
}

object UserActions {
  private val Shift = java.awt.event.InputEvent.SHIFT_DOWN_MASK
  private val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  val FollowAccel   = KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask)
  val UnfollowAccel = KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask | Shift)
  val BlockAccel    = KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask)  
  val UnblockAccel  = KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask | Shift)  
  val ReportSpamAccel = KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKeyMask | Shift)  
  val ViewListAccel = KeyStroke.getKeyStroke(KeyEvent.VK_L, Shift)  
}