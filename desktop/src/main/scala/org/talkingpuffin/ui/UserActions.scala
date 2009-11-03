package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
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
    }, UserActions.FollowAccel)
  }
  
  def unfollowAK(smi: SpecialMenuItems, getSelectedScreenNames: => List[String]) = {
    new ActionAndKeys(new Action("Unfollow") {
      def apply = unfollow(getSelectedScreenNames)
      smi.friendsOnly.list ::= this
    }, UserActions.UnfollowAccel)
  }
  
  def reportSpamAK(getSelectedScreenNames: => List[String]) = {
    new ActionAndKeys(Action("Report Spam") {reportSpam(getSelectedScreenNames)},
      UserActions.ReportSpamAccel)
  }
  
  def blockAK(getSelectedScreenNames: => List[String]) = {
    new ActionAndKeys(Action("Block") { block(getSelectedScreenNames) }, UserActions.BlockAccel)
  }
  
  def addCommonItems(mh: PopupMenuHelper, specialMenuItems: SpecialMenuItems, 
      table: JTable, getSelectedScreenNames: => List[String]) {
    mh add(Action("Show friends and followers") 
        {showFriends(getSelectedScreenNames)}, UserActions.ShowFriendsAccel)
    mh add(Action("View lists…") {viewLists(getSelectedScreenNames, table)}, UserActions.ViewListAccel)
    mh add(new TagAction(table, table.getModel.asInstanceOf[TaggingSupport]), ks(VK_T, 0))
    mh.add(followAK(specialMenuItems, getSelectedScreenNames))
    mh.add(unfollowAK(specialMenuItems, getSelectedScreenNames))
    mh.add(blockAK(getSelectedScreenNames))
    mh.add(reportSpamAK(getSelectedScreenNames))
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

  val FollowAccel   = ks(VK_F, shortcutKeyMask)
  val UnfollowAccel = ks(VK_F, shortcutKeyMask | Shift)
  val BlockAccel    = ks(VK_B, shortcutKeyMask)  
  val UnblockAccel  = ks(VK_B, shortcutKeyMask | Shift)  
  val ReportSpamAccel = ks(VK_S, shortcutKeyMask | Shift)  
  val ViewListAccel = ks(VK_L, Shift)  
  val ShowFriendsAccel = ks(VK_H, Shift)
  
}