package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.event.ButtonClicked
import filter.TextFilter
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.SpringLayout.Constraints
import scala.swing._

/**
 * A control for maintaining a list of TextFilters.
 * @author Dave Briccetti
 */

class TextFilterControl(label: String, textFilters: java.util.List[TextFilter]) extends BoxPanel(Orientation.Vertical) {
  border = BorderFactory.createTitledBorder(label)

  val tableModel = new TextFilterModel(textFilters)
  
  contents += new ScrollPane {
    contents = new Table {
      model = tableModel
      peer.getColumnModel().getColumn(0).setPreferredWidth(300)
      peer.getColumnModel().getColumn(1).setMaxWidth(60)
    }
    minimumSize = new Dimension(200, 100)
  }

  contents += new FlowPanel {
    val text = new TextField {columns=20; minimumSize=new Dimension(100, preferredSize.height)}
    contents += text 

    val regex = new CheckBox("Regex") {
      tooltip = "Whether this search argument is a regular expression"
    }
    contents += regex
    
    val newButton = new Button("New")
    contents += newButton
    
    val delAllButton = new Button("Delete All")
    contents += delAllButton
    
    listenTo(newButton)
    listenTo(delAllButton)
    
    reactions += {
      case ButtonClicked(`newButton`) => { 
        textFilters.add(new TextFilter(text.text, regex.peer.isSelected))
        dataChanged
      }
      case ButtonClicked(`delAllButton`) => { 
        textFilters.clear
        dataChanged
      }
    }
  }
  
  def dataChanged = tableModel.fireTableDataChanged 
    
}
  
