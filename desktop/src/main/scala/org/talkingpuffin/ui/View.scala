package org.talkingpuffin.ui

import java.awt.Point
import swing.{Reactor, TabbedPane}
import org.talkingpuffin.Session
import org.talkingpuffin.filter.{CompoundFilter, TagUsers, FilterSet, TextTextFilter}

case class View(val model: StatusTableModel, val pane: StatusPane) extends Reactor {
  listenTo(model)
  reactions += {
    case TableContentsChanged(model, filtered, total) =>
      pane.titleSuffix = if (total == 0) 
        "" 
      else 
        "(" + (if (total == filtered) 
          total 
        else 
          filtered + "/" + total) + ")"
  }
  
}

object View {
  def create(dataProvider: DataProvider, screenNameToUserNameMap: Map[String, String], 
      service: String, user: String, 
      tagUsers: TagUsers, session: Session, include: Option[String], viewCreator: ViewCreator,
      relationships: Relationships, location: Option[Point]): View = {
    val title = dataProvider.titleCreator.create
    val filterSet = new FilterSet(tagUsers)
    if (include.isDefined) {
      filterSet.includeSet.cpdFilters.list ::= new CompoundFilter( 
        List(TextTextFilter(include.get, false)), None, None)
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
    val pane = new StatusPane(session, title, title, model, filterSet, tagUsers, viewCreator)
    session.windows.tabbedPane.pages += new TabbedPane.Page(title, pane)
    if (location.isDefined) 
      pane.undock(location)
    new View(model, pane)
  }
}

