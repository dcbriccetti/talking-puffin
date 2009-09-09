package org.talkingpuffin.ui

import java.util.concurrent.{Executors, Callable}
import org.talkingpuffin.twitter.{AuthenticatedSession, TwitterUser}
import javax.swing.SwingWorker

object PeopleProvider {
  /**
   * Uses the provided AuthenticatedSession to get all friends and followers (doing the
   * fetches in parallel to be quicker), and provides the results by callback in the 
   * event-dispatching thread when done.
   */
  def get(twitterSession: AuthenticatedSession, edtCallback: (List[TwitterUser], List[TwitterUser]) => Unit) {
    val pool = Executors.newFixedThreadPool(2)
    val friendsFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFriends)
    })
    val followersFuture = pool.submit(new Callable[List[TwitterUser]] {
      def call = twitterSession.loadAll(twitterSession.getFollowers)
    })

    new SwingWorker[Tuple2[List[TwitterUser],List[TwitterUser]], Object] {
      def doInBackground = (friendsFuture.get, followersFuture.get)

      override def done = {
        val (friends, followers) = get 
        edtCallback(friends, followers)      
      }
    }.execute
    
  }
}
