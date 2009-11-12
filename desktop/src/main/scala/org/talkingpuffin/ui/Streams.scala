package org.talkingpuffin.ui

import java.awt.Point
import org.talkingpuffin.filter.{TagUsers}
import org.talkingpuffin.twitter.AuthenticatedSession
import org.talkingpuffin.Session
import java.util.prefs.Preferences
import util.ColTiler

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val twitterSession: AuthenticatedSession,
    val prefs: Preferences, val providers: DataProviders,
    session: Session, val tagUsers: TagUsers, val relationships: Relationships) 
    extends ViewCreator {
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
    view
  }
  
  def stop {
    providers.stop
  }

}
