package com.davebsoft.sctw.ui

import _root_.scala.xml.NodeSeq
import javax.swing.table.DefaultTableCellRenderer

/**
 * Renderer for the name column
 * @author Dave Briccetti
 */

class NameCellRenderer extends DefaultTableCellRenderer {
  override def setValue(value: Any) {
    val nodes = value.asInstanceOf[NodeSeq]
    setText((nodes \ "screen_name").text)
  }
}

