package com.davebsoft.sctw.ui

import java.awt.Desktop
import _root_.scala.xml.NodeSeq
import java.awt.event.{MouseEvent, MouseAdapter}

import java.net.URI
import javax.swing.event.{HyperlinkListener, HyperlinkEvent}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer}
import javax.swing.{JTextPane, JTable, JLabel}
/**
 * Renderer for the status column
 * @author Dave Briccetti
 */

class StatusCellRenderer extends DefaultTableCellRenderer {
  override def setValue(value: Object) {
    setText(value.asInstanceOf[String])
  }
}

class StatusCellFancyRenderer extends TableCellRenderer {
  private val label = new JLabel("Hi")
  
  val text = new JTextPane
  
  text.setContentType("text/html");
  text.setEditable(false);
  text.setBackground(label.getBackground());

  text.addHyperlinkListener(new HyperlinkListener() {
      def hyperlinkUpdate(e: HyperlinkEvent) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (Desktop.isDesktopSupported) {
              Desktop.getDesktop.browse(e.getURL().toURI)
            }
          }   
      }});
  text.addMouseMotionListener(new MouseAdapter{
    override def mouseMoved(e: MouseEvent) = println("mouse moved over text")
  });
  text.addMouseListener(new MouseAdapter{
    override def mouseClicked(e: MouseEvent) = {
      println("FR: " + e)
      println("HLL: " + text.getHyperlinkListeners)
    }
    
  });
  
  
  def getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, 
      hasFocus: Boolean, row: Int, column: Int) = {

    text.setText("<html>" + value.asInstanceOf[String] +  
            "<a href='http://davebsoft.com/'>A link</a></html>");
    text
  }
}

