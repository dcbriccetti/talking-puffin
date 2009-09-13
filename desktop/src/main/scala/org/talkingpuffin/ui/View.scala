package org.talkingpuffin.ui

import filter.{TagUsers, FilterSet, TextFilter}
import swing.TabbedPane

case class View(val title: String, val model: StatusTableModel, val pane: StatusPane)

object View {
  def create[T](dataProvider: DataProvider[T], usersTableModel: UsersTableModel, 
                service: String, user: String, 
                tagUsers: TagUsers, session: Session, include: Option[String], viewCreator: ViewCreator,
                followerIds: List[Long], friendIds: List[Long]): View = {
    val title = dataProvider.titleCreator.create
    val filterSet = new FilterSet(session, user, tagUsers)
    if (include.isDefined) {
      filterSet.includeTextFilters.list ::= new TextFilter(include.get, false) 
    }
    val sto = new StatusTableOptions(true, true, true)
    val model = dataProvider match {
      case p: MentionsProvider => new StatusTableModel(sto, p, usersTableModel,
        filterSet, service, user, tagUsers) with Mentions
      case p: BaseProvider => new StatusTableModel(sto, p, usersTableModel,
        filterSet, service, user, tagUsers)
    }
    val pane = new StatusPane(session, title, model, filterSet, tagUsers, viewCreator)
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    val view = new View(title, model, pane)
    view.model.followerIds = followerIds
    view.model.friendIds = friendIds
    view
  }
}

