package org.talkingpuffin.ui

import _root_.scala.swing._
import _root_.scala.swing.event.ButtonClicked
import filter.{TextFilters, TextFilter}
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.SpringLayout.Constraints
import javax.swing.{KeyStroke, BorderFactory}
import scala.swing._

/**         
 * A control for maintaining a list of TextFilters.
 */

class TextFilterControl(label: String, textFilters: TextFilters) extends BoxPanel(Orientation.Vertical) {
  border = BorderFactory.createTitledBorder(label)

  val tableModel = new TextFilterModel(textFilters)
  
  contents += new ScrollPane {
    contents = new Table {
      model = tableModel
      peer.getColumnModel().getColumn(0).setPreferredWidth(300)
      peer.getColumnModel().getColumn(1).setMaxWidth(60)

      def addDelete {
        val delAction = Action("Delete") {
          textFilters.list --= List.fromArray(for (i <- peer.getSelectedRows) yield textFilters.list(i))
          dataChanged
        }
        peer.getActionMap.put(delAction.title, delAction.peer)
        peer.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), delAction.title)
        peer.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), delAction.title)
      }

      addDelete
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
    
    listenTo(newButton, delAllButton)
    
    reactions += {
      case ButtonClicked(`newButton`) => {
        addTextFilter(text.text, regex.peer.isSelected)
      }
      case ButtonClicked(`delAllButton`) => { 
        textFilters.clear
        dataChanged
      }
    }
  }
  
  def addTextFilter(text: String, regex: Boolean) {
    textFilters.list ::= new TextFilter(text, regex)
    dataChanged
  }
  
  private def dataChanged = tableModel.fireTableDataChanged 
    
}
  
