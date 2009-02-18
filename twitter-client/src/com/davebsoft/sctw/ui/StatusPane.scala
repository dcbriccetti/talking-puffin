package com.davebsoft.sctw.ui

import java.awt.event.{ActionListener, ActionEvent}
import scala.swing._

/**
 * A status pane displays friend and public statuses
 */
class StatusPane(statusTableModel: StatusTableModel) extends BoxPanel(Orientation.Vertical) {
  
  contents += new ScrollPane {
    contents = new Table() {
      model = statusTableModel
      val colModel = peer.getColumnModel
      colModel.getColumn(0).setPreferredWidth(100)
      colModel.getColumn(1).setPreferredWidth(600)
    }
  }
  
  contents += new FlowPanel {
    contents += new Label("Refresh (secs)")
    val comboBox = new ComboBox(List.range(0, 50, 10) ::: List.range(60, 600, 60))
    var defaultRefresh = 120
    comboBox.peer.setSelectedItem(defaultRefresh)
    statusTableModel.setUpdateFrequency(defaultRefresh)
    comboBox.peer.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {  // Couldn’t get to work with reactions
        statusTableModel.setUpdateFrequency(comboBox.selection.item)
      }
    });
    contents += comboBox
  }

}