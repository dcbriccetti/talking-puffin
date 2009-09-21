package org.talkingpuffin.ui

import _root_.scala.swing.{ListView, Button, GridBagPanel, ScrollPane}
import filter.FilterSet
import javax.swing.event.{TableModelEvent, TableModelListener}
import javax.swing.BorderFactory
import java.awt.Dimension
import _root_.scala.swing.event.ButtonClicked
import _root_.scala.swing.GridBagPanel._

/**
 * A panel for unmuting any muted users
 */
class UnmutePane(title: String, tableModel: StatusTableModel, filterSet: FilterSet, 
    mutedList: scala.collection.mutable.Map[Long,User], unMute: (List[Long]) => Unit) 
    extends GridBagPanel with TableModelListener {
  border = BorderFactory.createTitledBorder(title)

  val view = new ListView(mutedList.values.toList)
  add(new ScrollPane(view) {
    val dim = new Dimension(150,130); preferredSize=dim; minimumSize=dim
  }, new Constraints {grid=(0,0); anchor=Anchor.West})

  tableModel.addTableModelListener(this)
  
  def tableChanged(e: TableModelEvent) = view.listData = mutedList.values.toList

  val removeButton = new Button("Remove")
  add(removeButton, new Constraints {grid=(0,1)})
  
  listenTo(removeButton)
  reactions += {
    case ButtonClicked(b) => unMute(view.selection.items.toList.map(_.id))
  }
}