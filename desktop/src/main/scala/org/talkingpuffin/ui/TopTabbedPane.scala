package org.talkingpuffin.ui

import org.talkingpuffin.Session
import swing.TabbedPane
import java.awt.Dimension

class TopTabbedPane(val session: Session) extends TabbedPane {
  preferredSize = new Dimension(900, 900)
  val tweetDetailPanel = new TweetDetailPanel(session, None)
}
