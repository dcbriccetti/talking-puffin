package org.talkingpuffin.ui.filter

import swing._
import scala.swing.GridBagPanel._
import java.awt.event.KeyEvent
import javax.swing.border.EmptyBorder
import org.talkingpuffin.filter.NoiseFilter

/**
 * The General pane of the Filters dialog.
 */
class GeneralPane extends GridBagPanel {
  border = new EmptyBorder(5, 5, 0, 5)

  class Cns(pos: Tuple2[Int,Int], gw: Int) extends Constraints {
    grid = pos; gridwidth = gw; anchor = Anchor.West
  }
  
  val excludeFriendRetweets = new CheckBox(
      "Exclude retweets of statuses of people you follow") {
    tooltip = "Requires a People view to have been created, which is not " +
      "recommended for huge sets of following/followers"
    peer.setMnemonic(KeyEvent.VK_R)
  }
  add(excludeFriendRetweets, new Cns((0, 1), 3))
  
  val excludeNonFollowers = new CheckBox("Exclude non-followers") {
    peer.setMnemonic(KeyEvent.VK_F)
  }
  add(excludeNonFollowers, new Cns((0, 2), 3))
  
  val useNoiseFilters = new CheckBox("Use external noise filters") {
    peer.setMnemonic(KeyEvent.VK_N)
  }
  add(useNoiseFilters, new Cns((0, 3), 1))
  
  add(new Button(new Action("Update") {
    mnemonic = KeyEvent.VK_O
    def apply = NoiseFilter.load
  }) {
    tooltip = "Fetch the latest noise filters from the external service"
  }, new Cns((1, 3), 1))
  
  add(new Label(" "), new Constraints { // Expanding filler
    grid = (1, 5); fill = Fill.Both; weightx = 1; weighty = 1
  })
}
  
