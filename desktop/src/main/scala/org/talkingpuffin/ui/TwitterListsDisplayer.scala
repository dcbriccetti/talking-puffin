package org.talkingpuffin.ui

import org.talkingpuffin.Session
import org.talkingpuffin.ui.util.Tiler
import swing.{Action, MenuItem}
import org.talkingpuffin.util.Parallelizer
import javax.swing.{JComponent, JPopupMenu, JTable}
import java.awt.Rectangle
import org.talkingpuffin.twitter.{Constants, TwitterArgs, TwitterUser, TwitterList}

case class MenuPos(parent: JComponent, menuX: Int, menuY: Int)

object TwitterListsDisplayer {

  type LLTL = List[List[TwitterList]]
  
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
  
  def viewLists(session: Session, lists: List[TwitterList]) = {
    lists.foreach(l => viewList(l, session, None))
  }
  
  def viewListsStatuses(session: Session, lists: List[TwitterList]) = {
    lists.foreach(l => viewListStatuses(l, session, None))
  }
  
  private def showListsInTable(session: Session)(lltl: LLTL): Unit = {
    new ListsFrame(session, lltl.flatMap(f => f))
  }
  
  private def viewList(list: TwitterList, session: Session, tiler: Option[Tiler]) = {
    SwingInvoke.execSwingWorker({
      val tsess = session.twitterSession
      tsess.loadAllWithCursor(tsess.getListMembers(list))}, {
      members: List[TwitterUser] => {
        session.windows.peoplePaneCreator.createPeoplePane(list.longName, 
          None, Some(members), None, tilerNext(tiler))
      }
    })
  }
  
  private def tilerNext(tiler: Option[Tiler]): Option[Rectangle] = 
    tiler match {case Some(t) => Some(t.next) case _ => None}

  private def viewListStatuses(list: TwitterList, session: Session, tiler: Option[Tiler]) = {
    val provider = new ListStatusesProvider(session, 
      list.owner.screenName, list.slug, None, session.progress)
    session.windows.streams.createView(session.desktopPane, provider, None, tilerNext(tiler))
    provider.loadContinually
  }
    
  private def getLists(session: Session, screenNames: List[String]): LLTL = {
    Parallelizer.run(20, screenNames, session.twitterSession.getLists) filter(_ != Nil)
  }
    
  private def getListsContaining(session: Session, screenNames: List[String]): LLTL = {
    val tsess = session.twitterSession
    def getAllMembershipsForScreenName(screenName: String): List[TwitterList] = {
      tsess.loadAllWithCursor(tsess.getListMemberships(screenName))
    }
    Parallelizer.run(20, screenNames, getAllMembershipsForScreenName) filter(_ != Nil)
  }
    
  private def processLists(vl: (TwitterList, Session, Option[Tiler]) => Unit)(
          showLongName: Boolean, session: Session, menuPos: MenuPos)(allListsOfLists: LLTL) = {
    if (allListsOfLists != Nil) {
      var numMenuItems = 0
      val menu = new JPopupMenu
      var combinedList = List[TwitterList]()
      allListsOfLists filter(_ != Nil) foreach(lists => {
        lists.foreach(twitterList => {
          menu.add(new MenuItem(Action(if (showLongName) 
              twitterList.longName else twitterList.shortName) {vl(twitterList, session, None)}).peer)
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