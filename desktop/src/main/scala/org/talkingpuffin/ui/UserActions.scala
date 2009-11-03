package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import javax.swing._
import javax.swing.KeyStroke.{getKeyStroke => ks}
import org.talkingpuffin.Session
import java.awt.{Toolkit}
import org.talkingpuffin.util.Loggable
import util.Tiler
import swing.Action

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
  
  def followAK(smi: SpecialMenuItems, getSelectedScreenNames: => List[String]) = {
    new ActionAndKeys(new Action("Follow") { 
      def apply = follow(getSelectedScreenNames)
      smi.notFriendsOnly.list ::= this
    }, ks(VK_F, UserActions.shortcutKeyMask))
  }
  
  def unfollowAK(smi: SpecialMenuItems, getSelectedScreenNames: => List[String]) = {
    new ActionAndKeys(new Action("Unfollow") {
      def apply = unfollow(getSelectedScreenNames)
      smi.friendsOnly.list ::= this
    }, ks(VK_F, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK))
  }
  
  def addCommonItems(mh: PopupMenuHelper, specialMenuItems: SpecialMenuItems, 
      table: JTable, showBigPicture: => Unit, getSelectedScreenNames: => List[String]) {

    mh add(new Action("Show larger image") { 
      def apply = showBigPicture
      specialMenuItems.oneStatusSelected.list ::= this
    }, ks(VK_I, 0))
    
    mh add(Action("Show friends and followers") 
        {showFriends(getSelectedScreenNames)}, ks(VK_H, SHIFT_DOWN_MASK))
    mh add(Action("View lists…") {viewLists(getSelectedScreenNames, table)}, ks(VK_L, SHIFT_DOWN_MASK))
    mh add(new TagAction(table, table.getModel.asInstanceOf[TaggingSupport]), ks(VK_T, 0))
    mh.add(followAK(specialMenuItems, getSelectedScreenNames))
    mh.add(unfollowAK(specialMenuItems, getSelectedScreenNames))
    mh.add(new ActionAndKeys(Action("Block") { block(getSelectedScreenNames) }, 
        ks(VK_B, UserActions.shortcutKeyMask)))
    mh.add(new ActionAndKeys(Action("Report Spam") {reportSpam(getSelectedScreenNames)},
        ks(VK_S, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK)))
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
  private val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  val UnblockAccel  = ks(VK_B, shortcutKeyMask | SHIFT_DOWN_MASK)  
}