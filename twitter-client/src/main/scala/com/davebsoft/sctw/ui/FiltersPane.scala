package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.event.ButtonClicked
import filter.TagsRepository
import java.awt.Dimension
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}
/**
 * Filters
 * @author Dave Briccetti
 */

class FiltersPane(tableModel: StatusTableModel) extends GridBagPanel {
  var selectedTags = List[String]()
  
  add(new Label("Tags"), new Constraints {gridx=0; gridy=0})
  add(new ScrollPane {
    preferredSize = new Dimension(100, 200)
    val listView = new ListView(TagsRepository.get)
    val selModel = listView.peer.getSelectionModel
    selModel.addListSelectionListener(new ListSelectionListener(){
      def valueChanged(e: ListSelectionEvent) = {
        if (! e.getValueIsAdjusting()) {
          selectedTags = List[String]()
          for (tag <- listView.peer.getSelectedValues) {
            selectedTags ::= tag.asInstanceOf[String]
          }
        }
      }
    })
    contents = listView
  }, new Constraints {gridx=0; gridy=1})

  val excludeNotToYouReplies = new CheckBox("Exclude replies not to you")
  add(excludeNotToYouReplies, new Constraints {gridx=0; gridy=2})
  listenTo(excludeNotToYouReplies)

  val applyButton = new Button("Apply")
  listenTo(applyButton)

  reactions += {
    case ButtonClicked(b) => {
      if (b == applyButton) {
        tableModel.selectedTags_$eq(selectedTags) // TODO Why doesn’t = work?
        tableModel.excludeNotToYouReplies_$eq(excludeNotToYouReplies.selected)
        tableModel.applyFilters
      }
    }
  }
  add(applyButton, new Constraints {gridx=0; gridy=5})
  add(new Label(""), new Constraints {gridx=1; gridy=0; fill=GridBagPanel.Fill.Horizontal; weightx=1; })
  add(new Label(""), new Constraints {gridx=0; gridy=6; fill=GridBagPanel.Fill.Vertical; weighty=1;})
}