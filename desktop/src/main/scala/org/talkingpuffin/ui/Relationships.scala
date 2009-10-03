package org.talkingpuffin.ui

import java.util.concurrent.{Executors, Callable}
import swing.event.Event
import swing.Publisher
import org.talkingpuffin.twitter.{TwitterUser, AuthenticatedSession, TwitterUserId}
import javax.swing.SwingWorker

case class IdsChanged() extends Event
case class UsersChanged() extends Event

class Relationships extends Publisher {
  var friendIds = List[Long]()
  var followerIds = List[Long]()
  var friends = List[TwitterUser]()
  var followers = List[TwitterUser]()

  /**
   * Uses the provided AuthenticatedSession to get all friends and followers (doing the
   * fetches in parallel to be quicker), and provides the results by callback in the 
   * event-dispatching thread when done.
   */
  def getUsers(twitterSession: AuthenticatedSession, longOpListener: LongOpListener) {
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFriends) })
    val followersFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFollowers) })

    new SwingWorker[Tuple2[List[TwitterUser],List[TwitterUser]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done { 
        val (fr, fo) = get
        friends = fr
        followers = fo
        longOpListener.stopOperation
        Relationships.this.publish(UsersChanged())
      }
    }.execute
  }
  
  def getIds(twitterSession: AuthenticatedSession, longOpListener: LongOpListener) {
    longOpListener.startOperation
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUserId]] {
      def call = twitterSession.getFriendsIds })
    val followersFuture = pool.submit(new Callable[List[TwitterUserId]] {
      def call = twitterSession.getFollowersIds })

    new SwingWorker[Tuple2[List[TwitterUserId],List[TwitterUserId]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)
      override def done { 
        val (fr, fo) = get
        friendIds = fr.map(_.id)
        followerIds = fo.map(_.id)
        longOpListener.stopOperation
        Relationships.this.publish(IdsChanged()) // SwingWorker also has a publish
      }
    }.execute
  }
  
  def removeFriendsWithScreenNames(names: List[String]) {
    friends = friends.filter(user => ! names.contains(user.screenName))
    friendIds = friends.map(_.id)
    publish(UsersChanged())
    publish(IdsChanged())
  }
  
}
  
