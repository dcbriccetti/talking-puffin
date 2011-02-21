package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import org.talkingpuffin.ui.util.Tiler
import swing.{Action, MenuItem}
import org.talkingpuffin.util.Parallelizer
import javax.swing.{JComponent, JPopupMenu, JTable}
import java.awt.Rectangle
import org.talkingpuffin.Session
import twitter4j.{UserList, User}
import org.talkingpuffin.twitter.PageHandler._

case class MenuPos(parent: JComponent, menuX: Int, menuY: Int)

object TwitterListsDisplayer {

  type LLUL = List[List[UserList]]
  
  def viewListsTable(session: Session, screenNames: List[String]): Unit =
    SwingInvoke.execSwingWorker({
      getLists(session, screenNames)
    }, showListsInTable(session))
  
  /**
   * Presents a pop-up menu of lists containing the specified screen names. One or all lists can 
   * be selected. Each list is launched in a new PeoplePane.
   */
  def viewListsContaining(session: Session, screenNames: List[String]) {
    SwingInvoke.execSwingWorker({getListsContaining(session, screenNames)}, 
        showListsInTable(session)) 
  }
  
  def viewLists(session: Session, lists: List[UserList]) = {
    lists.foreach(l => viewList(l, session, None))
  }
  
  def viewListsStatuses(session: Session, lists: List[UserList]) = {
    lists.foreach(l => viewListStatuses(l, session, None))
  }
  
  private def showListsInTable(session: Session)(lltl: LLUL): Unit = {
    new ListsFrame(session, lltl.flatMap(f => f))
  }
  
  private def viewList(list: UserList, session: Session, tiler: Option[Tiler]) = {
    SwingInvoke.execSwingWorker({
      val tw = session.twitter
      allPages(userListMembers(tw, list.getUser.getScreenName, list.getId))}, {
      members: List[User] => {
        session.peoplePaneCreator.createPeoplePane(list.getFullName, list.getName,
          None, Some(members), None, tilerNext(tiler))
      }
    })
  }
  
  private def tilerNext(tiler: Option[Tiler]): Option[Rectangle] =
    tiler match {case Some(t) => Some(t.next) case _ => None}

  private def viewListStatuses(list: UserList, session: Session, tiler: Option[Tiler]) = {
    val provider = new ListStatusesProvider(session, list, None, session.progress)
    session.streams.createView(session.desktopPane, provider, None, tilerNext(tiler))
    provider.loadContinually
  }

  private def getLists(session: Session, screenNames: List[String]): LLUL = {
    val tw = session.twitter
    def getMembers(screenName: String) = allPages(userLists(tw, screenName))
    Parallelizer.run(20, screenNames, getMembers) filter(_ != Nil)
  }
    
  private def getListsContaining(session: Session, screenNames: List[String]): LLUL = {
    val tw = session.twitter
    def getAllMembers(screenName: String) = allPages(userListMemberships(tw, screenName))
    Parallelizer.run(20, screenNames, getAllMembers) filter(_ != Nil)
  }
    
  private def processLists(vl: (UserList, Session, Option[Tiler]) => Unit)(
          showLongName: Boolean, session: Session, menuPos: MenuPos)(allListsOfLists: LLUL) = {
    if (allListsOfLists != Nil) {
      var numMenuItems = 0
      val menu = new JPopupMenu
      var combinedList = List[UserList]()
      allListsOfLists filter(_ != Nil) foreach(lists => {
        lists.foreach(twitterList => {
          menu.add(new MenuItem(Action(if (showLongName) 
              twitterList.getFullName else twitterList.getName) {vl(twitterList, session, None)}).peer)
          numMenuItems += 1
        })
        combinedList :::= lists
      })
      if (numMenuItems > 1) {
        val tiler = new Tiler(combinedList.length)
        menu.add(new MenuItem(Action("All") {
          combinedList.foreach(twitterList => vl(twitterList, session, Some(tiler)))
        }).peer) 
      }

      if (numMenuItems > 0) {
        menu.show(menuPos.parent, menuPos.menuX, menuPos.menuY)
      }
    }
  }

}