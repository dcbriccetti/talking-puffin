package org.talkingpuffin.ui

import java.awt.Rectangle
import java.util.prefs.Preferences
import javax.swing.JDesktopPane
import util.ColTiler
import swing.Reactor
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Session
import org.talkingpuffin.filter.{FilterSet, CompoundFilter, TextTextFilter, TagUsers}
import javax.swing.event.{InternalFrameEvent, InternalFrameAdapter}

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val prefs: Preferences, val session: Session, val tagUsers: TagUsers, val relationships: Relationships)
    extends Reactor with Loggable {
  val usersTableModel = new UsersTableModel(None, tagUsers, relationships)
  
  var views = List[View]()

  val dpSize = {
    val s1 = session.desktopPane.size
    if (s1.width > 0 && s1.height > 0) s1 else session.desktopPane.preferredSize 
  }
  val tiler = new ColTiler(dpSize, session.dataProviders.autoStartProviders.length, 1)
  session.dataProviders.autoStartProviders.foreach(provider => {
    createView(session.desktopPane, provider, None, Some(tiler.next))
    provider.loadContinually()
  })
  
  def createView(desktopPane: JDesktopPane, dataProvider: DataProvider, 
                 include: Option[String], location: Option[Rectangle]): View = {
    val screenNameToUserNameMap = usersTableModel.usersModel.screenNameToUserNameMap
    val sto = new StatusTableOptions(true, true, true)
    val filterSet = new FilterSet(tagUsers)
    val model = dataProvider match {
      case p: CommonTweetsProvider if p.statusTableModelCust.isDefined =>
        p.statusTableModelCust.get match {
          case StatusTableModelCust.Mentions => new StatusTableModel(session, sto, p, relationships,
              screenNameToUserNameMap, filterSet, tagUsers) with Mentions
        }
      case p: BaseProvider => new StatusTableModel(session, sto, p, relationships,
        screenNameToUserNameMap, filterSet, tagUsers)
    }
    val title = dataProvider.titleCreator.create
    if (include.isDefined) {
      filterSet.includeSet.cpdFilters.list ::= new CompoundFilter( 
        List(TextTextFilter(include.get, false)), None, None)
    }
    val pane = new StatusPane(session, title, model, filterSet, tagUsers)
    val frame = new TitledStatusFrame(pane, session.dataProviders, tagUsers, model)
    desktopPane.add(frame)
    if (location.isDefined) {
      frame.setBounds(location.get)
    } else {
      frame.setBounds(0, 0, 400, dpSize.height)
    }
    frame.moveToFront
    val view = new View(model, pane, Some(frame))
    views = views ::: List(view)
    frame.addInternalFrameListener(new InternalFrameAdapter {
      override def internalFrameClosing(e: InternalFrameEvent) = {
        debug(e.toString)
        views = views.filter(_.frame.get != e.getSource)
      }
    })
    view
  }
  
  def stop {
    session.dataProviders.stop
  }

}
