package org.talkingpuffin.ui

import java.awt.{Dimension}
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.{SwingUtilities, JToolBar}
import swing.{ProgressBar, Label, Action}
import util.ToolBarHelpers

/**
 * The main ToolBar
 */
class MainToolBar extends JToolBar with ToolBarHelpers with LongOpListener {
  val progressBar = new ProgressBar {
    val s = new Dimension(40, 0)
    preferredSize = s
    minimumSize = s
  }
  val operationsInProgress = new AtomicInteger
  val remaining = new Label
  var dataProvidersDialog: DataProvidersDialog = _

  setFloatable(false)

  def init(streams: Streams) = {
    dataProvidersDialog = new DataProvidersDialog(SwingUtilities.getAncestorOfClass(classOf[java.awt.Frame],
        MainToolBar.this).asInstanceOf[java.awt.Frame], streams)
    val dataProvidersAction = new Action("Data Providers") {
      toolTip = "Shows Data Providers Dialog"
      def apply = dataProvidersDialog.visible = true
    }

    aa(dataProvidersAction)
    addSeparator
    add(progressBar.peer)
  }
  
  def startOperation = if (operationsInProgress.incrementAndGet == 1) progressBar.indeterminate = true;
  
  def stopOperation = if (operationsInProgress.decrementAndGet == 0) progressBar.indeterminate = false;
  
}

