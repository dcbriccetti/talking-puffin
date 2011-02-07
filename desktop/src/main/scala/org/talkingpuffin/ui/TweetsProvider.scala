package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import scala.swing.event.Event
import org.talkingpuffin.util.Loggable
import twitter4j.Paging
import org.talkingpuffin.{Constants, Session}

case class NewTwitterDataEvent(data: List[AnyRef], clear: Boolean) extends Event

abstract class TweetsProvider(session: Session, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {

  override def getResponseId(response: TwitterDataWithId): Long = response.getId
}

class FollowingProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
  //def fn() =
  override def updateFunc = {() =>
    session.twitterSession.twitter.getHomeTimeline(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])}
}

class MentionsProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
  def fn() = session.twitterSession.twitter.getMentions(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

class RetweetsOfMeProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs of Me", longOpListener) {
  def fn() = session.twitterSession.twitter.getRetweetsOfMe(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

class RetweetedByMeProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs by Me", longOpListener) {
  def fn() = session.twitterSession.twitter.getRetweetedByMe(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

class RetweetedToMeProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs to Me", longOpListener) {
  def fn() = session.twitterSession.twitter.getRetweetedToMe(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

class DmsReceivedProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  def fn() = session.twitterSession.twitter.getDirectMessages(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

class DmsSentProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  def fn() = session.twitterSession.twitter.getSentDirectMessages(paging()).toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

class FavoritesProvider(session: Session, id: String, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, id + " Favorites", longOpListener) {
  def fn() = session.twitterSession.twitter.getFavorites.toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

/*todo class ListStatusesProvider(session: Session, userId: String, listId: String,
    startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, userId + " " + listId + " List", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getListStatusesFor(userId, listId)
}*/

