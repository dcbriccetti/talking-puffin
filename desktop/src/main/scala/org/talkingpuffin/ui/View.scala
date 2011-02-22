package org.talkingpuffin.ui

import swing.Reactor
import javax.swing.JComponent

case class View(model: StatusTableModel, pane: StatusPane, frame: Option[JComponent]) extends Reactor {
  listenTo(model)
  reactions += {
    case TableContentsChanged(model, filtered, total) =>
      pane.titleSuffix = if (total == 0)
        ""
      else
        "(" + (if (total == filtered)
          total
        else
          filtered + "/" + total) + ")"
  }
}
