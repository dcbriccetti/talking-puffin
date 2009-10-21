package org.talkingpuffin.ui

import org.talkingpuffin.filter.{TagUsers}
import org.talkingpuffin.state.{PreferencesFactory}
import org.talkingpuffin.twitter.AuthenticatedSession
import org.talkingpuffin.Session

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val twitterSession: AuthenticatedSession, 
    session: Session, val tagUsers: TagUsers, val relationships: Relationships) 
    extends ViewCreator {
  val prefs = PreferencesFactory.prefsForUser(service, twitterSession.user)
  val providers = new DataProviders(twitterSession, prefs, session.progress)
  val usersTableModel = new UsersTableModel(None, tagUsers, relationships)
  
  var views = List[View]()
  
  providers.providers.foreach(provider => {
    createView(provider, None)
    provider.loadNewData
  })
  
  def createView(dataProvider: DataProvider, include: Option[String]): View = {
    val view = View.create(dataProvider, usersTableModel.usersModel.screenNameToUserNameMap, service, 
      twitterSession.user, tagUsers, session, include, this, relationships)
    views ::= view
    view
  }
  
  def stop = {
    providers.stop
    views.foreach(_.stop)
  }

}
