package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.Toolkit
import javax.swing.{KeyStroke, JOptionPane}
import twitter.AuthenticatedSession

/**
 * Handles user actions like follow
 */
class UserActions(twitterSession: AuthenticatedSession, rels: Relationships) {
  def follow(names: List[String]) = process(names, 
      twitterSession.createFriendship, showFollowErr(_,"following",_))
  def unfollow(names: List[String]) = {
    process(names, twitterSession.destroyFriendship, showFollowErr(_, "unfollowing", _))
    rels.removeFriendsWithScreenNames(names)
  }
  def block  (names: List[String]) = process(names, twitterSession.blockUser  , showFollowErr(_,"block",_))
  def unblock(names: List[String]) = process(names, twitterSession.unblockUser, showFollowErr(_,"block",_))
  
  private def process(names:List[String], action:((String) => Unit), errHandler:((Throwable,String) => Unit)) = 
    names foreach {name => try {action(name)} catch {case e: Throwable => errHandler(e,name)}}

  private def showFollowErr(e:Throwable,action:String,screenName:String){
    JOptionPane.showMessageDialog(null, "Error " + action + " " + screenName)
  }
}

object UserActions {
  val FollowAccel   = KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask)
  val UnfollowAccel = KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask | Shift)
  val BlockAccel    = KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask)  
  val UnblockAccel  = KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask | Shift)  

  private val Shift = java.awt.event.InputEvent.SHIFT_DOWN_MASK
  private val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask
}