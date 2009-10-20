package org.talkingpuffin.ui

import org.talkingpuffin.Session
import javax.swing.{JPopupMenu, JTable}
import org.talkingpuffin.twitter.{TwitterUser, TwitterList}
import org.talkingpuffin.ui.util.Tiler
import swing.{Action, MenuItem}

object TwitterListsDisplayer {

  /**
   * Presents a pop-up menu of lists belonging to users with the specified screen names. One or
   * all lists can be selected. Each list is launched in a new PeoplePane.
   */
  def viewLists(session: Session, selectedScreenNames: List[String], table: JTable) {
    
    def viewList(list: TwitterList, tiler: Option[Tiler]) = {
      SwingInvoke.execSwingWorker({session.twitterSession.getListMembers(list)}, {
        members: List[TwitterUser] => {
          session.windows.peoplePaneCreator.createPeoplePane(list.longName, list.shortName,
            Some(members), None, true, tiler match {case Some(t) => Some(t.next) case _ => None})
        }
      })
    }
    
    SwingInvoke.execSwingWorker({
      var listsList = List[org.talkingpuffin.twitter.TwitterLists]()
      selectedScreenNames.foreach(screenName => {
        val lists = session.twitterSession.getLists(screenName)
        if (lists.xml != Nil)
          listsList ::= lists
      })
      listsList reverse
      }, {
        listsList: List[org.talkingpuffin.twitter.TwitterLists] => {
          if (listsList != Nil) {
            var numMenuItems = 0
            val menu = new JPopupMenu
            var combinedList = List[TwitterList]()
            listsList foreach(twitterLists => {
              val twLists = twitterLists.lists.toList
              if (twLists != Nil) {
                twLists.foreach(l => {
                  menu.add(new MenuItem(Action(
                    if (listsList.length == 1) l.shortName else l.longName) {viewList(l, None)}).peer)
                  numMenuItems += 1
                })
                combinedList :::= twLists
              }
            })
            if (numMenuItems > 1) {
              menu.add(new MenuItem(Action("All") {
                val tiler = new Tiler(combinedList.length)
                combinedList.foreach(twitterList => { viewList(twitterList, Some(tiler))})}).peer)
            }

            val menuLoc = table.getCellRect(table.getSelectedRow, 0, true).getLocation
            menu.show(table, menuLoc.getX().asInstanceOf[Int], menuLoc.getY().asInstanceOf[Int])
          }
        }
      })
  }
  
}