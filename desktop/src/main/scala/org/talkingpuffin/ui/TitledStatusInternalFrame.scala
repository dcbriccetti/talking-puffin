package org.talkingpuffin.ui

import org.talkingpuffin.filter.TagUsers
import javax.swing.event.{InternalFrameEvent, InternalFrameAdapter}
import javax.swing.{JComponent, JInternalFrame}
import java.awt.AWTEvent

/**
 * A frame for status panes, including a custom title and menu.
 */
class TitledStatusInternalFrame(val pane: StatusPane, providers: DataProviders, tagUsers: TagUsers,
    val model: StatusTableModel, close: (AWTEvent) => Unit) extends JInternalFrame(pane.longTitle, true, true, true, true)
{
  val setter = new StatusTitleSetter(pane, model, setTitle)
  setContentPane(pane.peer)
  setVisible(true)

  addInternalFrameListener(new InternalFrameAdapter {
    override def internalFrameClosing(e: InternalFrameEvent) = {
      setter.stop()
      close(e)
    }
  })
}

object TitledFrameFactory {
  def create(parentWindow: JComponent, pane: StatusPane, providers: DataProviders, tagUsers: TagUsers,
    model: StatusTableModel, close: (AWTEvent) => Unit): JComponent =
  {
    parentWindow match {
      case desktop: DesktopPane => {
        val frame = new TitledStatusInternalFrame(pane, providers, tagUsers, model, close)
        desktop.add(frame)
        frame
      }
    }
  }
}