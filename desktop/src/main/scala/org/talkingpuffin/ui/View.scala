package org.talkingpuffin.ui

import filter.{TagUsers, FilterSet, TextFilter}
import swing.TabbedPane

case class View(val title: String, val model: StatusTableModel, val pane: StatusPane)

object View {
  def create(dataProvider: DataProvider, screenNameToUserNameMap: Map[String, String], 
                service: String, user: String, 
                tagUsers: TagUsers, session: Session, include: Option[String], viewCreator: ViewCreator,
                relationships: Relationships): View = {
    val title = dataProvider.titleCreator.create
    val filterSet = new FilterSet(tagUsers)
    if (include.isDefined) {
      filterSet.includeSet.textFilters.list ::= new TextFilter(include.get, false) 
    }
    val sto = new StatusTableOptions(true, true, true)
    val model = dataProvider match {
      case p: MentionsProvider => new StatusTableModel(sto, p, relationships, screenNameToUserNameMap,
        filterSet, service, user, tagUsers) with Mentions
      case p: DmsSentProvider => new StatusTableModel(sto, p, relationships, screenNameToUserNameMap,
        filterSet, service, user, tagUsers) with DmsSent
      case p: BaseProvider => new StatusTableModel(sto, p, relationships, screenNameToUserNameMap,
        filterSet, service, user, tagUsers)
    }
    val pane = new StatusPane(session, title, model, filterSet, tagUsers, viewCreator)
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    val view = new View(title, model, pane)
    view
  }
}

