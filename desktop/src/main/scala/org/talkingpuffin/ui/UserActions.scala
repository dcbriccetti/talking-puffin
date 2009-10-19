package org.talkingpuffin.ui

import java.awt.event.KeyEvent
import swing.{MenuItem, Action}
import scala.xml.NodeSeq
import javax.swing._
import org.talkingpuffin.Session
import org.talkingpuffin.twitter.{TwitterUser}
import java.awt.{Point, Toolkit}
import org.talkingpuffin.util.Loggable
import java.util.concurrent.atomic.AtomicInteger

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
  
  def viewLists(selectedScreenNames: List[String], table: JTable) {
    def viewList(list: NodeSeq, tiler: Option[Tiler]) = {
      val listShortName = (list \ "name").text
      val listLongName = listShortName + " from " + (list \ "user" \ "name").text
      SwingInvoke.execSwingWorker({tsess.getListMembers(list)}, {
        members: List[TwitterUser] => {
          session.windows.peoplePaneCreator.createPeoplePane(listLongName, listShortName,
            Some(members), None, true, tiler match {case Some(t) => Some(t.next) case _ => None})
        }
      })
    }
    
    selectedScreenNames.foreach(screenName => {
      SwingInvoke.execSwingWorker({
        tsess.getLists(screenName)
      }, {
        listsNode: NodeSeq => {
          val lists = (listsNode \ "list").toList
          if (lists != Nil) {
            val menu = new JPopupMenu
            lists.foreach(l => {
              menu.add(new MenuItem(Action((l \ "name").text) {viewList(l, None)}).peer)
            })
            menu.add(new MenuItem(Action("All") {
              val tiler = new Tiler(lists.length)
              lists.foreach(l2 => { viewList(l2, Some(tiler))})}).peer)

            val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
            menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
          }
        }
      })
    })    
  }
  
  class Tiler(numTiles: Int) {
    private val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    private val cols = Math.sqrt(numTiles.toDouble).ceil.toInt
    private val tileHeight = screenSize.height / cols
    private val tileWidth = screenSize.width / cols
    private val nextTileIndex = new AtomicInteger(0)
    
    def next: Point = {
      val tileIndex = nextTileIndex.getAndIncrement
      val row = tileIndex / cols
      val col = tileIndex % cols
      new Point(col * tileWidth, row * tileHeight)
    } 
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