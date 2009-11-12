package org.talkingpuffin.ui

import java.awt.Point
import org.talkingpuffin.Session
import org.talkingpuffin.filter.{CompoundFilter, TagUsers, FilterSet, TextTextFilter}
import swing.{Reactor}

case class View(val model: StatusTableModel, val pane: StatusPane) extends Reactor 

object View {
  def create(dataProviders: DataProviders, dataProvider: DataProvider, 
      screenNameToUserNameMap: Map[String, String], service: String, user: String, 
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
    val pane = new StatusPane(session, title, model, filterSet, tagUsers, viewCreator)
    val frame = new TitledStatusFrame(title, session, dataProviders, tagUsers, model, pane)
    if (location.isDefined) 
      frame.location = location.get
    new View(model, pane)
  }
}

