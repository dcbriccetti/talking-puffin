package org.talkingpuffin.ui

import java.util.concurrent.atomic.AtomicInteger
import javax.swing.{SwingUtilities, JToolBar}
import swing.{ProgressBar, Label, Action}
import util.ToolBarHelpers
import java.awt.Dimension

/**
 * The main ToolBar
 */
class MainToolBar extends JToolBar with ToolBarHelpers with LongOpListener {
  private val progressBar = new ProgressBar {
    val s = new Dimension(40, 0)
    preferredSize = s
    minimumSize = s
  }
  val operationsInProgress = new AtomicInteger
  val remaining = new Label(" ")
  var dataProvidersDialog: DataProvidersDialog = _

  setFloatable(false)

  def init(streams: Streams) {
    dataProvidersDialog = new DataProvidersDialog(SwingUtilities.getAncestorOfClass(classOf[java.awt.Frame],
        MainToolBar.this).asInstanceOf[java.awt.Frame], streams)
    
    aa(new Action("Data Providers") {
      toolTip = "Shows Data Providers Dialog"
      def apply = dataProvidersDialog.visible = true
    })
    addSeparator
    add(new Label("Left: ") {tooltip = "The number of requests remaining in the hour, before reset"}.peer)
    add(remaining.peer)
    addSeparator
    add(progressBar.peer)
  }
  
  def startOperation = if (operationsInProgress.incrementAndGet == 1) progressBar.indeterminate = true;
  
  def stopOperation = if (operationsInProgress.decrementAndGet == 0) progressBar.indeterminate = false;
  
}
