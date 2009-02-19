package com.davebsoft.sctw.ui

import _root_.scala.swing.{ListView, Label, GridBagPanel, ScrollPane}
import filter.tagsRepository
import java.awt.Dimension
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
/**
 * Filters
 * @author Dave Briccetti
 */

class FiltersPane(tableModel: StatusTableModel) extends GridBagPanel {
  add(new Label("Tags"), new Constraints {gridx=0; gridy=0})
  add(new ScrollPane {
    preferredSize = new Dimension(100, 200)
    val listView = new ListView(tagsRepository.get)
    val selModel = listView.peer.getSelectionModel
    selModel.addListSelectionListener(new ListSelectionListener(){
      def valueChanged(e: ListSelectionEvent) = {
        if (! e.getValueIsAdjusting()) {
          var selectedTags = List[String]()
          for (tag <- listView.peer.getSelectedValues) {
            selectedTags ::= tag.asInstanceOf[String]
          }
          tableModel.setSelectedTags(selectedTags)
        }
      }
    })
    contents = listView
  }, new Constraints {gridx=0; gridy=1})
  add(new Label(""), new Constraints {gridx=1; gridy=0; fill=GridBagPanel.Fill.Horizontal; weightx=1; })
  add(new Label(""), new Constraints {gridx=0; gridy=2; fill=GridBagPanel.Fill.Vertical; weighty=1;})
}