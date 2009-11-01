package org.talkingpuffin.ui

import _root_.scala.swing.event.ButtonClicked
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.{KeyStroke, BorderFactory}
import scala.swing.{BoxPanel, Orientation, ScrollPane, Table, FlowPanel, Button, Action}
import org.talkingpuffin.filter.{CompoundFilter, CompoundFilters}
import _root_.org.talkingpuffin.util.Loggable

class CompoundFilterControl(label: String, filters: CompoundFilters) extends BoxPanel(Orientation.Vertical) with Loggable {
  border = BorderFactory.createTitledBorder(label)

  val tableModel = new CompoundFilterModel(filters)
  
  contents += new ScrollPane {
    contents = new Table {
      model = tableModel
      peer.getColumnModel().getColumn(0).setPreferredWidth(100)
      peer.getColumnModel().getColumn(1).setMaxWidth(15)
      peer.getColumnModel().getColumn(2).setPreferredWidth(100)
      peer.getColumnModel().getColumn(3).setMaxWidth(15)
      peer.getColumnModel().getColumn(4).setPreferredWidth(100)
      peer.getColumnModel().getColumn(5).setMaxWidth(15)
      peer.getColumnModel().getColumn(6).setPreferredWidth(100)
      peer.getColumnModel().getColumn(7).setMaxWidth(15)
      peer.getColumnModel().getColumn(8).setMaxWidth(30)

      def addDelete {
        val delAction = Action("Delete") {
          filters.list --= List.fromArray(for (i <- peer.getSelectedRows) yield filters.list(i))
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
    val newButton = new Button("New")
    contents += newButton
    
    val delAllButton = new Button("Delete All")
    contents += delAllButton
    
    listenTo(newButton, delAllButton)
    
    reactions += {
      case ButtonClicked(`newButton`) => {
        val dlg = new CompoundFilterDialog(addFilter)
        dlg.setLocationRelativeTo(this)
        dlg.visible = true
      }
      case ButtonClicked(`delAllButton`) => { 
        filters.clear
        dataChanged
      }
    }
  }
  
  def addFilter(filter: CompoundFilter) {
    filters.list = filters.list ::: List(filter)
    val i = filters.list.length - 1
    tableModel.fireTableRowsInserted(i, i)
  }
  
  private def dataChanged = tableModel.fireTableDataChanged

}
  
