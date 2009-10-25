package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import javax.swing._
import org.talkingpuffin.Session
import java.awt.{Toolkit}
import org.talkingpuffin.util.Loggable
import util.Tiler

/**
 * Handles user actions like follow
 */
class UserActions(session: Session, rels: Relationships) extends Loggable {
  val tsess = session.twitterSession
  
  def follow(names: List[String]) = process(names, tsess.createFriendship, "following")
  
  def unfollow(names: List[String]) {
    process(names, tsess.destroyFriendship, "unfollowing")
    rels.removeFriendsWithScreenNames(names)
  }

  def block(names: List[String]) {
    process(names, tsess.blockUser, "block")
    rels.removeFriendsWithScreenNames(names)
  }
  
  def unblock(names: List[String]) = process(names, tsess.unblockUser, "unblock")
  
  def reportSpam(names: List[String]) = process(names, tsess.reportSpam, "report spam")
  
  def viewLists(selectedScreenNames: List[String], table: JTable) = 
      TwitterListsDisplayer.viewLists(session, selectedScreenNames, table)
  
  def showFriends(selectedScreenNames: List[String]) = {
    val tiler = new Tiler(selectedScreenNames.length)
    selectedScreenNames.foreach(screenName => {
      val rels = new Relationships
      rels.getUsers(session.twitterSession, screenName, session.progress)
      session.windows.peoplePaneCreator.createPeoplePane("Friends and Followers of " + screenName, 
        screenName,
        Some(rels), None, None, false, Some(tiler.next))
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
  val ShowFriendsAccel = KeyStroke.getKeyStroke(KeyEvent.VK_H, Shift)  
}