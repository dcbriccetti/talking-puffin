package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import scala.swing.event.Event
import org.talkingpuffin.util.Loggable
import twitter4j.Paging
import org.talkingpuffin.{Constants, Session}
import org.talkingpuffin.twitter.TwitterArgs

case class NewTwitterDataEvent(data: List[AnyRef], clear: Boolean) extends Event

abstract class TweetsProvider(session: Session, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {

  override def getResponseId(response: TwitterDataWithId): Long = response.getId
}

class FollowingProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
  override def updateFunc(args: TwitterArgs) =
    session.twitterSession.twitter.getHomeTimeline(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}

class MentionsProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
  override def updateFunc(args: TwitterArgs) =
    session.twitterSession.twitter.getMentions(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}

class RetweetsOfMeProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs of Me", longOpListener) {
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getRetweetsOfMe(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}

class RetweetedByMeProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs by Me", longOpListener) {
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getRetweetedByMe(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}

class RetweetedToMeProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "RTs to Me", longOpListener) {
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getRetweetedToMe(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}

class FavoritesProvider(session: Session, id: String, startingId: Option[Long],
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, id + " Favorites", longOpListener) {
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getFavorites.toList.map(_.asInstanceOf[TwitterDataWithId])
}

/* todo class ListStatusesProvider(session: Session, userId: String, listId: String,
    startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, userId + " " + listId + " List", longOpListener) {
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getUserListStatuses(userId, listId, new Paging)
}*/

/* todo class DmsReceivedProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getDirectMessages(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}

class DmsSentProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  override def updateFunc(args: TwitterArgs) = session.twitterSession.twitter.getSentDirectMessages(paging(args.since)).toList.map(_.asInstanceOf[TwitterDataWithId])
}*/
