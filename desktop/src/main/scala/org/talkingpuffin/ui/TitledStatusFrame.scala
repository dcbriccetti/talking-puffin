package org.talkingpuffin.ui

import swing.Frame
import org.talkingpuffin.filter.TagUsers

/**
 * A frame for status panes, including a custom title and menu.
 */
class TitledStatusFrame(val pane: StatusPane,  
                        providers: DataProviders, tagUsers: TagUsers,
                        val model: StatusTableModel) extends Frame {
  title = pane.longTitle
  menuBar = new MainMenuBar(pane.session, tagUsers)
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
    if (titleSuffix.length == 0) pane.longTitle else pane.longTitle + " " + titleSuffix
}

