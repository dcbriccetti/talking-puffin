package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.ButtonClicked
import filter.TagsRepository
import java.awt.{Dimension, Insets}
import javax.swing.BorderFactory
import javax.swing.event.{ListSelectionListener, ListSelectionEvent, TableModelListener, TableModelEvent}

/**
 * Filters
 * @author Dave Briccetti
 */

class FiltersPane(tableModel: StatusTableModel) extends GridBagPanel {
  add(new FiltersSettingsPane(tableModel), new Constraints {gridx=0; gridy=0})
  add(new UnmutePane(tableModel), new Constraints {gridx=0; gridy=1})
  add(new Label(""), new Constraints {gridx=1; gridy=0; fill=Fill.Horizontal; weightx=1})
  add(new Label(""), new Constraints {gridx=0; gridy=1; fill=Fill.Vertical; weighty=1})
}

class FiltersSettingsPane(tableModel: StatusTableModel) extends GridBagPanel {
  border = BorderFactory.createTitledBorder("Settings")
  
  var selectedTags = List[String]()
  
  val tagsPanel = new GridBagPanel {
    add(new Label("Tags"), new Constraints {gridx=0; gridy=0})
  
    add(new ScrollPane {
      preferredSize = new Dimension(100, 170)
      minimumSize = new Dimension(100, 100)
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
  }
  
  add(tagsPanel, new Constraints {gridx=0; gridy=0; gridwidth=3})
  
  val excludeNotToYouReplies = new CheckBox("Exclude replies not to you")
  add(excludeNotToYouReplies, new Constraints {gridx=0; gridy=2; gridwidth=3})
  listenTo(excludeNotToYouReplies)
  
  class MatchField extends TextField {columns=20; minimumSize=new Dimension(100, preferredSize.height)}

  add(new Label("Include Matching"), new Constraints {gridx=0; gridy=3})
  val includeMatching = new MatchField
  add(includeMatching, new Constraints {gridx=1; gridy=3})
  
  val includeIsRegex = new CheckBox("Regular Expression")
  add(includeIsRegex, new Constraints {gridx=2; gridy=3})

  add(new Label("Exclude Matching"), new Constraints {gridx=0; gridy=4})
  val excludeMatching = new MatchField
  add(excludeMatching, new Constraints {gridx=1; gridy=4})

  val excludeIsRegex = new CheckBox("Regular Expression")
  add(excludeIsRegex, new Constraints {gridx=2; gridy=4})

  val applyButton = new Button("Apply")
  add(applyButton, new Constraints {gridx=0; gridy=20; gridwidth=3})
  listenTo(applyButton)

  reactions += {
    case ButtonClicked(b) => {
      if (b == applyButton) {
        tableModel.selectedTags = selectedTags
        tableModel.excludeNotToYouReplies = excludeNotToYouReplies.selected
        tableModel.setIncludeMatching(includeMatching.text, includeIsRegex.selected)
        tableModel.setExcludeMatching(excludeMatching.text, excludeIsRegex.selected)
        tableModel.applyFilters
      }
    }
  }
  add(new Label(""), new Constraints {gridx=4; gridy=0; fill=GridBagPanel.Fill.Horizontal; weightx=1; })
  add(new Label(""), new Constraints {gridx=0; gridy=21; fill=GridBagPanel.Fill.Vertical; weighty=1;})
}

class UnmutePane(tableModel: StatusTableModel) extends GridBagPanel with TableModelListener {
  border = BorderFactory.createTitledBorder("Muted users")

  val mutedUsersList = new ListView(tableModel.mutedUsers.values.toList)
  add(new ScrollPane {
    contents = mutedUsersList; preferredSize=new Dimension(150,130); minimumSize=new Dimension(150,130)
  }, new Constraints {gridx=0; gridy=0; anchor=Anchor.West})

  tableModel.addTableModelListener(this)
  
  def tableChanged(e: TableModelEvent) {
    mutedUsersList.listData = tableModel.mutedUsers.values.toList
  }

  val unmuteButton = new Button("Unmute")
  add(unmuteButton, new Constraints {gridx=0; gridy=1})
  
  listenTo(unmuteButton)
  reactions += {
    case ButtonClicked(b) => {
      val selected = mutedUsersList.selection.items.toList
      tableModel.unmuteUsers(selected.map(_.id))
    }
  }
} 