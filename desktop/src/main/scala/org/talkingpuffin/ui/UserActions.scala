package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent._
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import java.awt.event.InputEvent.ALT_DOWN_MASK
import swing.Action
import javax.swing.KeyStroke.{getKeyStroke => ks}
import javax.swing.JTable
import java.awt.{Toolkit}
import org.talkingpuffin.Session
import org.talkingpuffin.util.Loggable
import util.Tiler
import twitter4j.Paging

/**
 * Handles user actions like follow
 */
class UserActions(val session: Session, rels: Relationships) extends ActionProcessor with Loggable {
  val tw = session.twitter
  type Names = List[String]

  def follow(names: Names) = processUsers(names, tw.createFriendship, "following", "Now following %s.")
  
  def unfollow(names: Names) {
    processUsers(names, tw.destroyFriendship, "unfollowing", "Unfollowed %s.")
    rels.removeFriendsWithScreenNames(names)
  }

  def block(names: Names) {
    processUsers(names, tw.createBlock, "block", "%s blocked.")
    rels.removeFriendsWithScreenNames(names)
  }
  
  def unblock(names: Names) = processUsers(names, tw.destroyBlock, "unblock", "%s unblocked.")
  
  def reportSpam(names: Names) = processUsers(names, tw.reportSpam, "report spam", "%s reported for spam.")
  
  def viewLists(selectedScreenNames: Names, table: JTable) =
    TwitterListsDisplayer.viewListsTable(session, selectedScreenNames)

  def viewListsOn(selectedScreenNames: Names, table: JTable) =
    TwitterListsDisplayer.viewListsContaining(session, selectedScreenNames)

  def showFriends(selectedScreenNames: Names) = {
    val tiler = new Tiler(selectedScreenNames.length)
    selectedScreenNames.foreach(screenName => {
      val rels = new Relationships
      rels.getUsers(session, screenName, session.progress)
      session.windows.peoplePaneCreator.createPeoplePane("Friends and Followers of " + screenName, 
        Some(rels), None, None, Some(tiler.next))
    })
  }
  
  def showFavorites(selectedScreenNames: Names) = {
    val tiler = new Tiler(selectedScreenNames.length)
    selectedScreenNames.foreach(screenName => {
      val favorites = new FavoritesProvider(session, screenName, None, session.progress)
      session.windows.streams.createView(session.desktopPane, favorites, None, Some(tiler.next))
      favorites.loadAndPublishData(new Paging, false)
    })
  }
  
  def followAK(smi: SpecialMenuItems, getSelectedScreenNames: => Names) = {
    new ActionAndKeys(new Action("Follow") { 
      def apply = follow(getSelectedScreenNames)
      smi.notFriendsOnly.list ::= this
    }, ks(VK_F, UserActions.shortcutKeyMask))
  }
  
  def unfollowAK(smi: SpecialMenuItems, getSelectedScreenNames: => Names) = {
    new ActionAndKeys(new Action("Unfollow") {
      def apply = unfollow(getSelectedScreenNames)
      smi.friendsOnly.list ::= this
    }, ks(VK_F, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK))
  }

  def addCommonItems(mh: PopupMenuHelper, specialMenuItems: SpecialMenuItems, 
      table: JTable, showBigPicture: => Unit, getSelectedScreenNames: => Names) {

    mh add(new Action("Show larger image") { 
      def apply = showBigPicture
      specialMenuItems.oneStatusSelected.list ::= this
    }, ks(VK_I, 0))
    
    mh add(Action("Show friends and followers") 
        {showFriends(getSelectedScreenNames)}, ks(VK_H, SHIFT_DOWN_MASK))
    mh add(Action("Show favorites") 
        {showFavorites(getSelectedScreenNames)}, ks(VK_H, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK))
    mh add(Action("View lists…") {viewLists(getSelectedScreenNames, table)}, ks(VK_L, SHIFT_DOWN_MASK))
    mh add(Action("View lists on…") {viewListsOn(getSelectedScreenNames, table)}, 
        ks(VK_L, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK))
    mh add(new TagAction(table, table.getModel.asInstanceOf[TaggingSupport]), ks(VK_T, 0))
    mh.add(followAK(specialMenuItems, getSelectedScreenNames))
    mh.add(unfollowAK(specialMenuItems, getSelectedScreenNames))
    mh.add(new ActionAndKeys(Action("Block") { block(getSelectedScreenNames) }, 
        ks(VK_B, UserActions.shortcutKeyMask)))
    mh.add(new ActionAndKeys(Action("Report Spam") {reportSpam(getSelectedScreenNames)},
        ks(VK_S, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK)))
  }
}

object UserActions {
  private val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  val UnblockAccel  = ks(VK_B, shortcutKeyMask | SHIFT_DOWN_MASK)  
}