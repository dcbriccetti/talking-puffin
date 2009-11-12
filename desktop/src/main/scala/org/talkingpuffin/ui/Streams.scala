package org.talkingpuffin.ui

import java.awt.Point
import org.talkingpuffin.filter.{TagUsers}
import org.talkingpuffin.twitter.AuthenticatedSession
import org.talkingpuffin.Session
import java.util.prefs.Preferences
import util.ColTiler
import swing.Reactor
import swing.event.WindowClosing

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val twitterSession: AuthenticatedSession,
    val prefs: Preferences, val providers: DataProviders,
    session: Session, val tagUsers: TagUsers, val relationships: Relationships) 
    extends ViewCreator with Reactor {
  val usersTableModel = new UsersTableModel(None, tagUsers, relationships)
  
  var views = List[View]()
  
  val tiler = new ColTiler(providers.autoStartProviders.length)
  providers.autoStartProviders.foreach(provider => {
    createView(provider, None, Some(tiler.next))
    provider.loadContinually()
  })
  
  def createView(dataProvider: DataProvider, include: Option[String], location: Option[Point]): View = {
    val view = View.create(providers, dataProvider, usersTableModel.usersModel.screenNameToUserNameMap, service, 
      twitterSession.user, tagUsers, session, include, this, relationships, location)
    views ::= view
    if (view.frame.isDefined)
      listenTo(view.frame.get)
    reactions += {
      case wc: WindowClosing => views = views.filter(_.frame.get != wc.source)
    }
    view
  }
  
  def stop {
    providers.stop
  }

}
