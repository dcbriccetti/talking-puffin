package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import scala.swing.event.Event
import scala.xml.{NodeSeq}
import twitter4j.{Status}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Session

case class NewTwitterDataEvent(val data: List[AnyRef], val clear: Boolean) extends Event

abstract class TweetsProvider(session: Session, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
}

class FollowingProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
  def fn(): List[TwitterDataWithId] =
    session.twitterSession.twitter.getHomeTimeline.toList.map(_.asInstanceOf[TwitterDataWithId])
  override def updateFunc = fn
}

/*
class MentionsProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getReplies
}

class RetweetsOfMeProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs of Me", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getRetweetsOfMe
}

class RetweetedByMeProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs by Me", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getRetweetedByMe
}

class RetweetedToMeProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs to Me", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getRetweetedToMe
}

class DmsReceivedProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  def updateFunc:() => List[DirectMessage] = session.twitterSession.getDirectMessages
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class DmsSentProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  def updateFunc:() => List[DirectMessage] = session.twitterSession.getSentMessages
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class FavoritesProvider(session: Session, id: String, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, id + " Favorites", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getFavoritesFor(id)
}

class ListStatusesProvider(session: Session, userId: String, listId: String, 
    startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, userId + " " + listId + " List", longOpListener) {
  override def updateFunc:() => List[Status] = session.twitterSession.getListStatusesFor(userId, listId)
}
*/
case class TweetsArrived(tweets: NodeSeq) extends Event
