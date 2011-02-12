package org.talkingpuffin.ui

import scala.collection.JavaConversions._
import scala.swing.event.Event
import twitter4j.{Status, ResponseList, Paging}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.Session
import org.talkingpuffin.twitter.TwitterArgs

case class NewTwitterDataEvent(data: List[AnyRef], clear: Boolean) extends Event

abstract class TweetsProvider(session: Session, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {

  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  protected def toIdData(r: ResponseList[Status]): List[TwitterDataWithId] =
    r.toList.map(_.asInstanceOf[TwitterDataWithId])
}

class CommonTweetsProvider(title: String, session: Session, startingId: Option[Long], longOpListener: LongOpListener,
    twFunc: (Paging) => ResponseList[Status],
    val statusTableModelCust: Option[StatusTableModelCust.Value] = None)
    extends TweetsProvider(session, startingId, title, longOpListener) {
  override def updateFunc(args: TwitterArgs) = toIdData(twFunc(paging(args.since)))
}

class FavoritesProvider(session: Session, id: String, startingId: Option[Long],
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, id + " Favorites", longOpListener) {
  override def updateFunc(args: TwitterArgs) = toIdData(tw.getFavorites)
}

/* todo class ListStatusesProvider(session: Session, userId: String, listId: String,
    startingId: Option[Long], longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, userId + " " + listId + " List", longOpListener) {
  override def updateFunc(args: TwitterArgs) = toIdData(tw.getUserListStatuses(userId, listId, new Paging))
}*/

/* todo class DmsReceivedProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  override def updateFunc(args: TwitterArgs) = toIdData(tw.getDirectMessages(paging(args.since)))
}

class DmsSentProvider(session: Session, startingId: Option[Long], longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  override def getResponseId(response: TwitterDataWithId): Long = response.getId
  override def updateFunc(args: TwitterArgs) = toIdData(tw.getSentDirectMessages(paging(args.since)))
}*/
