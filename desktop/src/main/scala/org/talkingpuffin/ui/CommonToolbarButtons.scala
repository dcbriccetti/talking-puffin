package org.talkingpuffin.ui

import javax.swing.JToggleButton
import swing.{Panel, Action}

class CommonToolbarButtons {
  private var detailsButton: JToggleButton = _

  def createDetailsButton(detailPanel: => Panel): JToggleButton = {
    val showDetailsAction: Action = new Action("Details") {
      toolTip = "Shows or hides the details panel"
      def apply = detailPanel.visible = detailsButton.isSelected
    }
    detailsButton = new JToggleButton(showDetailsAction.peer)
    detailsButton.setSelected(true)
    detailsButton
  }

}
