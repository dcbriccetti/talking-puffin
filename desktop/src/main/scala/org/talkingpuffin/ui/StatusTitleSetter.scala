package org.talkingpuffin.ui

import swing.Reactor

/**
 * Listens to a table model, and sets a title when the contents change.
 */
class StatusTitleSetter(pane: StatusPane, model: StatusTableModel, setTitle: (String) => Unit) extends Reactor {
  listenTo(model)
  reactions += {
    case TableContentsChanged(model, filtered, total) =>
      val titleSuffix = if (total == 0)
        None
      else
        Some("(" + (if (total == filtered) total else filtered + "/" + total) + ")")
      setTitle(withSuffix(titleSuffix))
  }

  def stop() = deafTo(model)

  private def withSuffix(titleSuffix: Option[String]) =
    if (titleSuffix.isDefined) pane.longTitle + " " + titleSuffix.get else pane.longTitle
}

