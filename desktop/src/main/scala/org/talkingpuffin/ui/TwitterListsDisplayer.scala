package org.talkingpuffin.ui

import org.talkingpuffin.Session
import org.talkingpuffin.ui.util.Tiler
import swing.{Action, MenuItem}
import org.talkingpuffin.util.Parallelizer
import javax.swing.{JComponent, JPopupMenu, JTable}
import org.talkingpuffin.twitter.{TwitterUser, TwitterList}

case class MenuPos(parent: JComponent, menuX: Int, menuY: Int)

object TwitterListsDisplayer {

  /**
   * Presents a pop-up menu of lists belonging to users with the specified screen names. One or
   * all lists can be selected. Each list is launched in a new PeoplePane.
   */
  def viewLists(session: Session, screenNames: List[String], menuPos: MenuPos) {
    SwingInvoke.execSwingWorker({getLists(session, screenNames)}, 
        {processLists(screenNames.length > 1, session, menuPos)}) 
  }
  
  /**
   * Presents a pop-up menu of lists containing the specified screen names. One or all lists can 
   * be selected. Each list is launched in a new PeoplePane.
   */
  def viewListsContaining(session: Session, screenNames: List[String], menuPos: MenuPos) {
    SwingInvoke.execSwingWorker({getListsContaining(session, screenNames)}, 
        {processLists(true, session, menuPos)}) 
  }
  
  private def viewList(list: TwitterList, session: Session, tiler: Option[Tiler]) = {
    SwingInvoke.execSwingWorker({
      val tsess = session.twitterSession
      tsess.loadAllWithCursor(tsess.getListMembers(list))}, {
      members: List[TwitterUser] => {
        session.windows.peoplePaneCreator.createPeoplePane(list.longName, 
          None, Some(members), None, true, tiler match {case Some(t) => Some(t.next) case _ => None})
      }
    })
  }
    
  private def getLists(session: Session, screenNames: List[String]): List[List[TwitterList]] = {
    Parallelizer.run(20, screenNames, session.twitterSession.getLists) filter(_ != Nil)
  }
    
  private def getListsContaining(session: Session, screenNames: List[String]): List[List[TwitterList]] = {
    val tsess = session.twitterSession
    def getAllMembershipsForScreenName(screenName: String): List[TwitterList] = {
      tsess.loadAllWithCursor(tsess.getListMemberships(screenName))
    }
    Parallelizer.run(20, screenNames, getAllMembershipsForScreenName) filter(_ != Nil)
  }
    
  private def processLists(showLongName: Boolean, session: Session, menuPos: MenuPos)(allListsOfLists: 
      List[List[TwitterList]]) = {
    if (allListsOfLists != Nil) {
      var numMenuItems = 0
      val menu = new JPopupMenu
      var combinedList = List[TwitterList]()
      allListsOfLists filter(_ != Nil) foreach(lists => {
        lists.foreach(twitterList => {
          menu.add(new MenuItem(Action(if (showLongName) 
              twitterList.longName else twitterList.shortName) {viewList(twitterList, session, None)}).peer)
          numMenuItems += 1
        })
        combinedList :::= lists
      })
      if (numMenuItems > 1) {
        val tiler = new Tiler(combinedList.length)
        menu.add(new MenuItem(Action("All") {
          combinedList.foreach(twitterList => viewList(twitterList, session, Some(tiler)))
        }).peer) 
      }

      if (numMenuItems > 0) {
        menu.show(menuPos.parent, menuPos.menuX, menuPos.menuY)
      }
    }
  }
    
}