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
  val filterSettingsPane = new FiltersSettingsPane(tableModel)
  add(filterSettingsPane, new Constraints {grid=(0,0)})
  add(new Label(""), new Constraints {grid=(1,0); fill=Fill.Horizontal; weightx=1})
  add(new Label(""), new Constraints {grid=(0,1); fill=Fill.Vertical; weighty=1})
  
  def applyChanges = filterSettingsPane.applyChanges
}

class FiltersSettingsPane(tableModel: StatusTableModel) extends GridBagPanel {
  var selectedTags = List[String]()
  
  val tagsPanel = new GridBagPanel {
    add(new Label("Tags"), new Constraints {grid=(0,0)})
  
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
    }, new Constraints {grid=(0,1)})
  }
  
  add(new FlowPanel {
    contents += tagsPanel
    contents += new UnmutePane(tableModel)
  }, new Constraints {grid=(0,0); gridwidth=3})
  
  val excludeNotToYouReplies = new CheckBox("Exclude replies not to you")
  add(excludeNotToYouReplies, new Constraints {grid=(0,2); gridwidth=3})
  
  class MatchField extends TextField {columns=20; minimumSize=new Dimension(100, preferredSize.height)}

  add(new Label("Include"), new Constraints {grid=(0,3)})
  val includeMatching = 
    new MatchField {tooltip="Include only tweets that match this string or regular expression"}
  add(includeMatching, new Constraints {grid=(1,3)})
  
  class RegexCheckBox extends CheckBox("Regex") {
    tooltip = "Whether this search argument is a regular expression"
  }
  
  val includeIsRegex = new RegexCheckBox
  add(includeIsRegex, new Constraints {grid=(2,3)})

  add(new Label("Exclude"), new Constraints {grid=(0,4)})
  val excludeMatching = new MatchField {tooltip="Exclude tweets that match this string or regular expression"}
  add(excludeMatching, new Constraints {grid=(1,4)})

  val excludeIsRegex = new RegexCheckBox
  add(excludeIsRegex, new Constraints {grid=(2,4)})

  add(new Label(""), new Constraints {grid=(4,0); fill=GridBagPanel.Fill.Horizontal; weightx=1; })
  add(new Label(""), new Constraints {grid=(0,21); fill=GridBagPanel.Fill.Vertical; weighty=1;})
  
  def applyChanges {
    tableModel.selectedTags = selectedTags
    tableModel.excludeNotToYouReplies = excludeNotToYouReplies.selected
    tableModel.setIncludeMatching(includeMatching.text, includeIsRegex.selected)
    tableModel.setExcludeMatching(excludeMatching.text, excludeIsRegex.selected)
    tableModel.applyFilters
  }
}

 