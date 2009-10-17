package org.talkingpuffin.ui

import _root_.scala.swing.{TabbedPane}
import org.talkingpuffin.util.Loggable

/**
 * Window manager.
 */
class Windows extends Loggable {
  var tabbedPane: TabbedPane = _
  var streams: Streams = _
  var peoplePaneCreator: PeoplePaneCreator = _
    
}
 