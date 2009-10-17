package org.talkingpuffin.ui

import java.util.concurrent.{Executors, Callable}
import swing.event.Event
import swing.Publisher
import org.talkingpuffin.twitter.{TwitterUser, AuthenticatedSession, TwitterUserId}
import javax.swing.SwingWorker

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
  def getUsers(twitterSession: AuthenticatedSession, longOpListener: LongOpListener) {
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAllWithCursor(twitterSession.getFriends) })
    val followersFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAllWithCursor(twitterSession.getFollowers) })

    new SwingWorker[Tuple2[List[TwitterUser],List[TwitterUser]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val (fr, fo) = get
          friends = fr
          followers = fo
          Relationships.this.publish(UsersChanged(Relationships.this))
          }, "Error fetching friends and followers for " + twitterSession.user)
      }
    }.execute
  }
  
  def getIds(twitterSession: AuthenticatedSession, longOpListener: LongOpListener) {
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUserId]] {
      def call = twitterSession.loadAllWithCursor(twitterSession.getFriendsIds) })
    val followersFuture = pool.submit(new Callable[List[TwitterUserId]] {
      def call = twitterSession.loadAllWithCursor(twitterSession.getFollowersIds) })

    new SwingWorker[Tuple2[List[TwitterUserId],List[TwitterUserId]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done {
        longOpListener.stopOperation
        doAndHandleError(() => {
          val (fr, fo) = get
          friendIds = fr.map(_.id)
          followerIds = fo.map(_.id)
          Relationships.this.publish(IdsChanged(Relationships.this)) // SwingWorker also has a publish
        }, "Error fetching friend and follower IDs for " + twitterSession.user)
      }
    }.execute
  }
  
  def removeFriendsWithScreenNames(names: List[String]) {
    friends = friends.filter(user => ! names.contains(user.screenName))
    friendIds = friends.map(_.id)
    publish(UsersChanged(this))
    publish(IdsChanged(this))
  }
  
}
  
