package org.talkingpuffin.ui

import org.talkingpuffin.filter.TagUsers
import javax.swing.JInternalFrame
import javax.swing.event.{InternalFrameEvent, InternalFrameAdapter}

/**
 * A frame for status panes, including a custom title and menu.
 */
class TitledStatusInternalFrame(val pane: StatusPane, providers: DataProviders, tagUsers: TagUsers,
    val model: StatusTableModel) extends JInternalFrame(pane.longTitle, true, true, true, true)
{
  val setter = new StatusTitleSetter(pane, model, setTitle)
  setContentPane(pane.peer)
  setVisible(true)

  addInternalFrameListener(new InternalFrameAdapter {
    override def internalFrameClosing(e: InternalFrameEvent) = setter.stop()
  })
}
