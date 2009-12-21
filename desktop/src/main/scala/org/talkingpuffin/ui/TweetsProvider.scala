package org.talkingpuffin.ui

import _root_.scala.swing.event.Event
import _root_.scala.xml.{NodeSeq}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.twitter.{TwitterMessage, AuthenticatedSession, TwitterArgs, TwitterStatus}
import org.talkingpuffin.Session

case class NewTwitterDataEvent(val data: List[AnyRef], val clear: Boolean) extends Event

abstract class TweetsProvider(session: Session, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class FollowingProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getHomeTimeline
}

class MentionsProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getReplies
}

class RetweetsOfMeProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs of Me", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getRetweetsOfMe
}

class RetweetedByMeProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs by Me", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getRetweetedByMe
}

class RetweetedToMeProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs to Me", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getRetweetedToMe
}

class DmsReceivedProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  def updateFunc:(TwitterArgs) => List[TwitterMessage] = session.twitterSession.getDirectMessages
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class DmsSentProvider(session: Session, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  def updateFunc:(TwitterArgs) => List[TwitterMessage] = session.twitterSession.getSentMessages
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class FavoritesProvider(session: Session, id: String, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, id + " Favorites", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getFavoritesFor(id)
}

class ListStatusesProvider(session: Session, userId: String, listId: String, 
    startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, userId + " " + listId + " List", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.twitterSession.getListStatusesFor(userId, listId)
}

case class TweetsArrived(tweets: NodeSeq) extends Event
