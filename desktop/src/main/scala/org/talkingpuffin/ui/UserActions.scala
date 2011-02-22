package org.talkingpuffin.ui

import java.awt.event.KeyEvent._
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import swing.Action
import javax.swing.KeyStroke.{getKeyStroke => ks}
import javax.swing.JTable
import java.awt.Toolkit
import org.talkingpuffin.Session
import org.talkingpuffin.twitter.PageHandler._
import org.talkingpuffin.util.{LinkUnIndirector, Loggable}
import util.{DesktopUtil, Tiler}
import twitter4j.Status

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

  def showUserTimeline(screenName: String) =
    createView(new UserTweetsProvider(session, screenName, session.progress))

  def showFriends(screenName: String) = {
    val rels = new Relationships
    rels.getUsers(session, screenName, session.progress)
    session.peoplePaneCreator.createPeoplePane("Friends and Followers of " + screenName, screenName,
      Some(rels), None, None, None)
  }
  
  def showFavorites(screenName: String) =
    createView(new FavoritesProvider(session, screenName, None, session.progress))
  
  def forAll(selectedScreenNames: Names, fn: (String) => Unit) = {
    selectedScreenNames.foreach(screenName => fn(screenName))
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
      table: JTable, showBigPicture: => Unit,
      getSelectedScreenNames: (Boolean) => Names, getSelectedStatuses: (Boolean) => List[Status]) {

    def names = getSelectedScreenNames(true)

    mh add(new Action("Show larger image") { 
      def apply = showBigPicture
      specialMenuItems.oneStatusSelected.list ::= this
    }, ks(VK_I, 0))
    
    mh add(Action("View user timeline") {forAll(names, showUserTimeline)}, ks(VK_T, SHIFT_DOWN_MASK))
    mh add(Action("Show friends and followers") {forAll(names, showFriends)}, ks(VK_H, SHIFT_DOWN_MASK))
    mh add(Action("Show favorites")
        {forAll(names, showFavorites)}, ks(VK_H, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK))
    mh add(Action("View lists…") {viewLists(names, table)}, ks(VK_L, SHIFT_DOWN_MASK))
    mh add(Action("View lists on…") {viewListsOn(names, table)},
        ks(VK_L, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK))
    mh add(new TagAction(table, table.getModel.asInstanceOf[TaggingSupport]), ks(VK_T, 0))
    mh add(followAK(specialMenuItems, names))
    mh add(unfollowAK(specialMenuItems, names))
    mh add(new ActionAndKeys(Action("Block") {block(names)}, ks(VK_B, UserActions.shortcutKeyMask)))
    mh add(new ActionAndKeys(Action("Report Spam") {reportSpam(names)},
        ks(VK_S, UserActions.shortcutKeyMask | SHIFT_DOWN_MASK)))
    def getSelStat(): Option[Status] = {
      getSelectedStatuses(true) match {
        case status :: others => Some(status)
        case _ => None
      }
    }
    mh add(new OpenPageLinksAction(getSelStat, table,
      LinkUnIndirector.findLinks(DesktopUtil.browse, DesktopUtil.browse)), ks(VK_L, 0))
    mh add(new OpenTwitterUserLinksAction(getSelStat, table, DesktopUtil.browse), ks(VK_U, 0))
    mh add(new OpenTwitterUserListsAction(getSelStat, table, DesktopUtil.browse), ks(VK_U, SHIFT_DOWN_MASK))
  }

  private def createView(provider: DataProvider): Unit = {
    session.streams.createView(session.tabbedPane, provider, None, None)
    provider.loadAndPublishData(newPagingMaxPer, false)
  }
}

object UserActions {
  private val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  val UnblockAccel  = ks(VK_B, shortcutKeyMask | SHIFT_DOWN_MASK)  
}