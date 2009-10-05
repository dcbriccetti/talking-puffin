package org.talkingpuffin.ui

import _root_.scala.swing.event.Event
import _root_.scala.xml.{NodeSeq}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.twitter.{TwitterMessage, AuthenticatedSession, TwitterArgs, TwitterStatus}

case class NewTwitterDataEvent(val data: List[AnyRef], val clear: Boolean) extends Event

abstract class TweetsProvider(session: AuthenticatedSession, startingId: Option[Long], 
    providerName: String, longOpListener: LongOpListener) extends
    DataProvider(session, startingId, providerName, longOpListener) with Loggable {
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class FollowingProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Following", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.getFriendsTimeline
}

class MentionsProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends TweetsProvider(session, startingId, "Mentions", longOpListener) {
  override def updateFunc:(TwitterArgs) => List[TwitterStatus] = session.getReplies
}

class DmsReceivedProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Rcvd", longOpListener) {
  def updateFunc:(TwitterArgs) => List[TwitterMessage] = session.getDirectMessages
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

class DmsSentProvider(session: AuthenticatedSession, startingId: Option[Long], 
    longOpListener: LongOpListener)
    extends DataProvider(session, startingId, "DMs Sent", longOpListener) {
  def updateFunc:(TwitterArgs) => List[TwitterMessage] = session.getSentMessages
  override def getResponseId(response: TwitterDataWithId): Long = response.id
}

case class TweetsArrived(tweets: NodeSeq) extends Event
