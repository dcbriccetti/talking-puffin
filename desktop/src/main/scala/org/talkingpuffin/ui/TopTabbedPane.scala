package org.talkingpuffin.ui

import org.talkingpuffin.Session
import swing.TabbedPane

class TopTabbedPane(val session: Session) extends TabbedPane {
  val tweetDetailPanel = new TweetDetailPanel(session, None)
}
