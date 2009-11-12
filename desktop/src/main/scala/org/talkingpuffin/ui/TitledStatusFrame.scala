package org.talkingpuffin.ui

import swing.Frame
import org.talkingpuffin.filter.TagUsers
import org.talkingpuffin.Session

/**
 * A frame for status panes, including a custom title and menu.
 */
class TitledStatusFrame(baseTitle: String, session: Session, 
                        providers: DataProviders, tagUsers: TagUsers,
                        val model: StatusTableModel, val pane: StatusPane) extends Frame {
  title = baseTitle
  menuBar = new MainMenuBar(session, providers, tagUsers)
  listenTo(model)
  reactions += {
    case TableContentsChanged(model, filtered, total) =>
      val titleSuffix = if (total == 0) 
        "" 
      else 
        "(" + (if (total == filtered) 
          total 
        else 
          filtered + "/" + total) + ")"
      title = withSuffix(titleSuffix)
  }

  contents = pane
  visible = true

  private def withSuffix(titleSuffix: String) = 
    if (titleSuffix.length == 0) baseTitle else baseTitle + " " + titleSuffix
}

