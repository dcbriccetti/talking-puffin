package org.talkingpuffin.ui

import java.util.concurrent.{Executors, Callable}
import swing.event.Event
import swing.Publisher
import org.talkingpuffin.twitter.{TwitterUser, AuthenticatedSession, TwitterUserId}
import javax.swing.SwingWorker
import org.talkingpuffin.Session

case class IdsChanged(val source: Relationships) extends Event
case class UsersChanged(val source: Relationships) extends Event

class Relationships extends Publisher with ErrorHandler {
  var friendIds = List[Long]()
  var followerIds = List[Long]()
  var friends = List[TwitterUser]()
  var followers = List[TwitterUser]()

  /**
   * Uses the provided AuthenticatedSession to get all friends and followers (doing the
   * fetches in parallel to be quicker), and publishes the results in the event-dispatching thread when done.
   */
  def getUsers(session: Session, screenName: String, longOpListener: LongOpListener) {
    val twSess = session.twitterSession
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twSess.loadAllWithCursor(twSess.getFriendsFor(screenName)) })
    val followersFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twSess.loadAllWithCursor(twSess.getFollowersFor(screenName)) })

    new SwingWorker[Tuple2[List[TwitterUser],List[TwitterUser]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val (fr, fo) = get
          friends = fr
          followers = fo
          Relationships.this.publish(UsersChanged(Relationships.this))
          }, "Error fetching friends and followers for " + twSess.user, session)
      }
    }.execute
    pool.shutdown()
  }
  
  def getIds(session: Session, longOpListener: LongOpListener) {
    val twSess = session.twitterSession
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUserId]] {
      def call = twSess.loadAllWithCursor(twSess.getFriendsIds) })
    val followersFuture = pool.submit(new Callable[List[TwitterUserId]] {
      def call = twSess.loadAllWithCursor(twSess.getFollowersIds) })

    new SwingWorker[Tuple2[List[TwitterUserId],List[TwitterUserId]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val (fr, fo) = get
          friendIds = fr.map(_.id)
          followerIds = fo.map(_.id)
          Relationships.this.publish(IdsChanged(Relationships.this)) // SwingWorker also has a publish
        }, "Error fetching friend and follower IDs for " + twSess.user, session)
      }
    }.execute
    pool.shutdown()
  }
  
  def removeFriendsWithScreenNames(names: List[String]) {
    friends = friends.filter(user => ! names.contains(user.screenName))
    friendIds = friends.map(_.id)
    publish(UsersChanged(this))
    publish(IdsChanged(this))
  }
  
}
  
