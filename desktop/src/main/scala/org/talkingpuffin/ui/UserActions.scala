package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.Toolkit
import javax.swing.{KeyStroke, JOptionPane}
import org.talkingpuffin.twitter.AuthenticatedSession

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
}