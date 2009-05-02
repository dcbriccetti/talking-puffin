package com.davebsoft.sctw.ui

import _root_.scala.swing.{Frame, TabbedPane, Component}
import javax.swing.{JFrame, SwingUtilities}
/**
 * Window manager.
 * 
 * @author Dave Briccetti
 */

class Windows {
  var tabbedPane: TabbedPane = _
  var streams: Streams = _
    
  def undock(comp: Component) {
    val frame = new Frame {
        title = streams.componentTitle(comp)
        contents = comp
      }
      frame.visible = true
    }
  
  def dock(comp: Component) = {
    val frame = SwingUtilities.getAncestorOfClass(classOf[JFrame], comp.peer).asInstanceOf[JFrame]
    tabbedPane.pages += new TabbedPane.Page(streams.componentTitle(comp), comp)
    frame.dispose
  }

}
 