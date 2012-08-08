package org.talkingpuffin.ui

import javax.swing.JComponent
import java.awt.Rectangle
import twitter4j.{UserList, User}
import SwingInvoke.execSwingWorker
import org.talkingpuffin.ui.util.Tiler
import org.talkingpuffin.util.Parallelizer
import org.talkingpuffin.apix.PageHandler._
import org.talkingpuffin.Session

case class MenuPos(parent: JComponent, menuX: Int, menuY: Int)

object TwitterListsDisplayer {

  type LLUL = Seq[Seq[UserList]]
  
  def viewListsTable(session: Session, screenNames: Iterable[String]) {
    execSwingWorker({
      getLists(session, screenNames)
    }, showListsInTable(session))
  }
  
  /**
   * Presents a pop-up menu of lists containing the specified screen names. One or all lists can 
   * be selected. Each list is launched in a new PeoplePane.
   */
  def viewListsContaining(session: Session, screenNames: Iterable[String]) {
    execSwingWorker({getListsContaining(session, screenNames)},
        showListsInTable(session))
  }
  
  def viewLists(session: Session, lists: List[UserList]) {
    lists.foreach(viewList(_, session, None))
  }
  
  def viewListsStatuses(session: Session, lists: List[UserList]) {
    lists.foreach(viewListStatuses(_, session, None))
  }
  
  def importLists(session: Session, lists: List[UserList]) {
    lists.foreach(importList(_, session))
  }

  private def showListsInTable(session: Session)(lltl: LLUL) {
    new ListsFrame(session, lltl.flatten)
  }
  
  private def viewList(list: UserList, session: Session, tiler: Option[Tiler]) {
    execSwingWorker({
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

  private def viewListStatuses(list: UserList, session: Session, tiler: Option[Tiler]) {
    val provider = new ListStatusesProvider(session, list, None, session.progress)
    session.streams.createView(session.tabbedPane, provider, None, tilerNext(tiler))
    provider.loadContinually()
  }

  private def importList(list: UserList, session: Session) {
    execSwingWorker({ allPages(userListMembers(session.twitter, list.getUser.getScreenName, list.getId)) }, {
      members: List[User] => {
        members.foreach(member => session.tagUsers.add(list.getName, member.getId))
      }
    })
    if (session.tagUsers.getDescription(list.getName).isEmpty) {
      session.tagUsers.addDescription(list.getName, list.getDescription)
    }
  }

  private def getLists(session: Session, screenNames: Iterable[String]): LLUL = {
    val tw = session.twitter
    def getMembers(screenName: String) = allPages(userLists(tw, screenName))
    Parallelizer.run(20, screenNames, getMembers, "Get lists").filter(_ != Nil).toSeq
  }
    
  private def getListsContaining(session: Session, screenNames: Iterable[String]): LLUL = {
    val tw = session.twitter
    def getAllMembers(screenName: String) = allPages(userListMemberships(tw, screenName))
    Parallelizer.run(20, screenNames, getAllMembers, "Get lists containing").filter(_ != Nil).toSeq
  }
}