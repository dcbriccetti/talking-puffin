package com.davebsoft.sctw.ui

import _root_.scala.swing.{ListView, Button, GridBagPanel, ScrollPane}
import filter.FilterSet
import javax.swing.event.{TableModelEvent, TableModelListener}
import javax.swing.BorderFactory
import java.awt.Dimension
import _root_.scala.swing.event.ButtonClicked
import javax.swing.SpringLayout.Constraints
import _root_.scala.swing.GridBagPanel._

/**
 * A panel for unmuting any muted users
 * @author Dave Briccetti
 */

class UnmutePane(tableModel: StatusTableModel, filterSet: FilterSet) extends GridBagPanel with TableModelListener {
  border = BorderFactory.createTitledBorder("Muted users")

  val mutedUsersList = new ListView(filterSet.mutedUsers.values.toList)
  add(new ScrollPane {
    val dim = new Dimension(150,130)
    contents = mutedUsersList; preferredSize=dim; minimumSize=dim
  }, new Constraints {grid=(0,0); anchor=Anchor.West})

  tableModel.addTableModelListener(this)
  
  def tableChanged(e: TableModelEvent) {
    mutedUsersList.listData = filterSet.mutedUsers.values.toList
  }

  val unmuteButton = new Button("Unmute")
  add(unmuteButton, new Constraints {grid=(0,1)})
  
  listenTo(unmuteButton)
  reactions += {
    case ButtonClicked(b) => {
      val selected = mutedUsersList.selection.items.toList
      tableModel.unmuteUsers(selected.map(_.id))
    }
  }
}