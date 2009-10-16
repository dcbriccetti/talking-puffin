package org.talkingpuffin.ui

import _root_.scala.swing.{Component, Reactor}
import org.talkingpuffin.filter.{TagUsers}
import javax.swing.{JFrame, JComponent, SwingUtilities}
import org.talkingpuffin.state.{PreferencesFactory}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.twitter.AuthenticatedSession
import org.talkingpuffin.Session

/**
 * Stream creation and management. A stream is a provider, model, filter set and view of tweets.
 */
class Streams(val service: String, val twitterSession: AuthenticatedSession, 
    session: Session, val tagUsers: TagUsers, val relationships: Relationships) 
    extends Reactor with ViewCreator with Loggable {
  val prefs = PreferencesFactory.prefsForUser(service, twitterSession.user)
  val providers = new DataProviders(twitterSession, prefs, session.progress)
  val usersTableModel = new UsersTableModel(None, tagUsers, relationships)
  
  var views = List[View]()
  
  reactions += {
    case TableContentsChanged(model, filtered, total) => 
      views.find(_.model == model) match {
        case Some(view) => setTitleInParent(view.pane.peer, view.title + " (" + 
            (if (total == filtered) total else filtered + "/" + total) + ")")
        case _ => info("No view found for model " + model) 
      }
  }

  providers.providers.foreach(provider => {
    createView(provider, None)
    provider.loadNewData
  })
  
  private def setTitleInParent(pane: JComponent, title: String) =
    session.windows.tabbedPane.peer.indexOfComponent(pane) match {
      case -1 => 
        SwingUtilities.getAncestorOfClass(classOf[JFrame], pane) match {
          case null =>
          case jf => jf.asInstanceOf[JFrame].setTitle(title) 
        }
      case i => session.windows.tabbedPane.peer.setTitleAt(i, title)
    }

  def createView(dataProvider: DataProvider, include: Option[String]): View = {
    val view = View.create(dataProvider, usersTableModel.usersModel.screenNameToUserNameMap, service, 
      twitterSession.user, tagUsers, session, include, this, relationships)
    listenTo(view.model)
    views ::= view
    view
  }

  def componentTitle(comp: Component) = views.filter(s => s.pane == comp)(0).title
  
}
