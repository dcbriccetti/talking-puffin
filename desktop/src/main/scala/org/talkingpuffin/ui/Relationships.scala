package org.talkingpuffin.ui

import java.util.concurrent.{Executors, Callable}
import javax.swing.SwingWorker
import swing.event.Event
import swing.Publisher
import org.talkingpuffin.Session
import org.talkingpuffin.model.BaseRelationships

case class IdsChanged(source: Relationships) extends Event
case class UsersChanged(source: Relationships) extends Event

class Relationships extends BaseRelationships with Publisher with ErrorHandler {

  /**
   * Uses the provided Twitter to get all friends and followers (doing the
   * fetches in parallel to be quicker), and publishes the results in the event-dispatching thread when done.
   */
  def getUsers(session: Session, screenName: String, longOpListener: LongOpListener) {
    val tw = session.twitter
    longOpListener.startOperation

    new SwingWorker[Tuple2[Users,Users], Object] {
      def doInBackground = getUsers(tw)

      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val (fr, fo) = get
          friends = fr
          followers = fo
          Relationships.this.publish(UsersChanged(Relationships.this))
          }, "Error fetching friends and followers for " + tw.getScreenName, session)
      }
    }.execute
  }

  def getIds(session: Session, longOpListener: LongOpListener) {
    val tw = session.twitter
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[Int]] {
      def call = {tw.getFriendsIDs.getIDs.toList}
    })
    val followersFuture = pool.submit(new Callable[List[Int]] {
      def call = {tw.getFollowersIDs.getIDs.toList}
    })

    new SwingWorker[Tuple2[List[Int],List[Int]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val (fr, fo) = get
          friendIds = fr map {_.toLong}
          followerIds = fo map {_.toLong}
          Relationships.this.publish(IdsChanged(Relationships.this)) // SwingWorker also has a publish
        }, "Error fetching friend and follower IDs for " + tw.getScreenName, session)
      }
    }.execute
    pool.shutdown()
  }
  
  def removeFriendsWithScreenNames(names: List[String]) {
    friends = friends.filter(user => ! names.contains(user.getScreenName))
    friendIds = friends.map(_.getId.toLong)
    publish(UsersChanged(this))
    publish(IdsChanged(this))
  }
  
}
  
