package org.talkingpuffin.ui

import java.awt.Rectangle
import java.util.prefs.Preferences
import util.ColTiler
import swing.Reactor
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Session
import org.talkingpuffin.filter.{FilterSet, CompoundFilter, TextTextFilter, TagUsers}
import swing.event.{WindowEvent, WindowClosing}

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val prefs: Preferences, 
    val session: Session, val tagUsers: TagUsers, val relationships: Relationships) 
    extends Reactor with Loggable {
  val usersTableModel = new UsersTableModel(None, tagUsers, relationships)
  
  var views = List[View]()
  
  val tiler = new ColTiler(session.dataProviders.autoStartProviders.length, 1d)
  session.dataProviders.autoStartProviders.foreach(provider => {
    createView(provider, None, Some(tiler.next))
    provider.loadContinually()
  })
  
  def createView(dataProvider: DataProvider, include: Option[String], location: Option[Rectangle]): View = {
    val screenNameToUserNameMap = usersTableModel.usersModel.screenNameToUserNameMap
    val user = session.twitterSession.user
    val sto = new StatusTableOptions(true, true, true)
    val filterSet = new FilterSet(tagUsers)
    val model = dataProvider match {
      case p: MentionsProvider => new StatusTableModel(sto, p, relationships, screenNameToUserNameMap,
        filterSet, service, user, tagUsers) with Mentions
      case p: DmsSentProvider => new StatusTableModel(sto, p, relationships, screenNameToUserNameMap,
        filterSet, service, user, tagUsers) with DmsSent
      case p: BaseProvider => new StatusTableModel(sto, p, relationships, screenNameToUserNameMap,
        filterSet, service, user, tagUsers)
    }
    val title = dataProvider.titleCreator.create
    if (include.isDefined) {
      filterSet.includeSet.cpdFilters.list ::= new CompoundFilter( 
        List(TextTextFilter(include.get, false)), None, None)
    }
    val pane = new StatusPane(session, title, model, filterSet, tagUsers)
    val frame = new TitledStatusFrame(pane, session.dataProviders, tagUsers, model)
    if (location.isDefined) {
      frame.peer.setBounds(location.get)
    }
    val view = new View(model, pane, Some(frame))
    views ::= view
    if (view.frame.isDefined)
      listenTo(view.frame.get)
    reactions += {
      case wc: WindowClosing => 
        debug(wc.toString)
        views = views.filter(_.frame.get != wc.source)
      case e: WindowEvent => debug(e.toString)
    }
    view
  }
  
  def stop {
    session.dataProviders.stop
  }

}
