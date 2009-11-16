package org.talkingpuffin.ui

import org.talkingpuffin.filter.TagUsers
import javax.swing.JInternalFrame
import swing.Reactor

/**
 * A frame for status panes, including a custom title and menu.
 */
class TitledStatusFrame(val pane: StatusPane,  
                        providers: DataProviders, tagUsers: TagUsers,
                        val model: StatusTableModel) extends JInternalFrame(pane.longTitle,
                        true, true, true, true) with Reactor {
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
      setTitle(withSuffix(titleSuffix))
  }

  setContentPane(pane.peer)
  setVisible(true)

  private def withSuffix(titleSuffix: String) = 
    if (titleSuffix.length == 0) pane.longTitle else pane.longTitle + " " + titleSuffix
}

