package org.talkingpuffin.ui

import org.talkingpuffin.Session
import org.talkingpuffin.ui.util.Tiler
import swing.{Action, MenuItem}
import org.talkingpuffin.twitter.{TwitterUser, TwitterList}
import org.talkingpuffin.util.Parallelizer
import javax.swing.{JComponent, JPopupMenu, JTable}

object TwitterListsDisplayer {

  /**
   * Presents a pop-up menu of lists belonging to users with the specified screen names. One or
   * all lists can be selected. Each list is launched in a new PeoplePane.
   */
  def viewLists(session: Session, screenNames: List[String], parent: JComponent, menuX: Int, menuY: Int) {

    val tsess = session.twitterSession
    
    def viewList(list: TwitterList, tiler: Option[Tiler]) = {
      SwingInvoke.execSwingWorker({tsess.loadAllWithCursor(tsess.getListMembers(list))}, {
        members: List[TwitterUser] => {
          session.windows.peoplePaneCreator.createPeoplePane(list.longName, list.shortName,
            None, Some(members), None, true, tiler match {case Some(t) => Some(t.next) case _ => None})
        }
      })
    }
    
    def getLists: List[List[TwitterList]] = {
      Parallelizer.run(20, screenNames, tsess.getLists) filter(_ != Nil)
    }
    
    SwingInvoke.execSwingWorker({getLists}, { 
      allListsOfLists: List[List[TwitterList]] => {
        if (allListsOfLists != Nil) {
          var numMenuItems = 0
          val menu = new JPopupMenu
          var combinedList = List[TwitterList]()
          allListsOfLists filter(_ != Nil) foreach(lists => {
            lists.foreach(twitterList => {
              menu.add(new MenuItem(Action(if (screenNames.length == 1) 
                  twitterList.shortName else twitterList.longName) {viewList(twitterList, None)}).peer)
              numMenuItems += 1
            })
            combinedList :::= lists
          })
          if (numMenuItems > 1) {
            val tiler = new Tiler(combinedList.length)
            menu.add(new MenuItem(Action("All") {combinedList.foreach(twitterList => {
              viewList(twitterList, Some(tiler))})}).peer) }

          if (numMenuItems > 0) {
            menu.show(parent, menuX, menuY)
          }
        }
      }
    })
  }
  
}