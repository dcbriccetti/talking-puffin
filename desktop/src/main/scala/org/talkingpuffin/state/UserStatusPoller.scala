package org.talkingpuffin.state

import swing.Publisher
import swing.event.Event
import javax.swing.Timer
import org.talkingpuffin.ui.SwingInvoke
import org.talkingpuffin.twitter.{TwitterRateLimitStatus, AuthenticatedSession}
import java.awt.event.{ActionEvent, ActionListener}

class UserStatusPoller(session: AuthenticatedSession) extends Publisher {
  private val timer = new Timer(60 * 1000, new ActionListener {
    def actionPerformed(p1: ActionEvent) = poll
  })
  timer.start
  
  poll // Poll once now, and timer will drive the rest
  
  def stop = timer.stop

  private def poll {
    SwingInvoke.execSwingWorker(session.getUserRateLimitStatus, 
      (status: TwitterRateLimitStatus) => UserStatusPoller.this.publish(RateLimitStatusEvent(status)))
  }
}

case class RateLimitStatusEvent(status: TwitterRateLimitStatus) extends Event