package org.talkingpuffin.ui

import org.talkingpuffin.Session
import swing.Action
import util.eventDistributor

object Tile {

  def actions(session: Session, long: Boolean = true) = {
    def rows(num: Int) = " row" + (if (num == 1) "" else "s")
    def title(num: Int) = if (long) "Tile, " + num + rows(num) else "T" + num
    List.range(1,4).map(num =>
      new Action(title(num)) {
        toolTip = "Tile windows into " + num + rows(num)
        def apply = eventDistributor.publish(TileViewsEvent(session, num))
      })
  }
}