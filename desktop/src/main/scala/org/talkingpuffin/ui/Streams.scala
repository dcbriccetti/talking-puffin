package org.talkingpuffin.ui

import java.awt.Point
import org.talkingpuffin.filter.{TagUsers}
import org.talkingpuffin.state.GlobalPrefs
import org.talkingpuffin.twitter.AuthenticatedSession
import org.talkingpuffin.Session

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val twitterSession: AuthenticatedSession, 
    session: Session, val tagUsers: TagUsers, val relationships: Relationships) 
    extends ViewCreator {
  val prefs = GlobalPrefs.prefsForUser(service, twitterSession.user)
  val providers = new DataProviders(twitterSession, prefs, session.progress)
  val usersTableModel = new UsersTableModel(None, tagUsers, relationships)
  
  var views = List[View]()
  
  providers.autoStartProviders.foreach(provider => {
    createView(provider, None, None)
    provider.loadContinually()
  })
  
  def createView(dataProvider: DataProvider, include: Option[String], location: Option[Point]): View = {
    val view = View.create(dataProvider, usersTableModel.usersModel.screenNameToUserNameMap, service, 
      twitterSession.user, tagUsers, session, include, this, relationships, location)
    views ::= view
    view
  }
  
  def stop = {
    providers.stop
  }

}
