package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.ButtonClicked
import filter.{TagsRepository, FilterSet, TextFilter, FilterSetChanged}
import java.awt.{Dimension, Insets}
import javax.swing.BorderFactory
import javax.swing.event.{ListSelectionListener, ListSelectionEvent, TableModelListener, TableModelEvent}

/**
 * Filters
 * @author Dave Briccetti
 */

class FiltersPane(tableModel: StatusTableModel, filterSet: FilterSet) extends GridBagPanel {
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
            var selectedTags = List[String]()
            for (tag <- listView.peer.getSelectedValues) {
              selectedTags ::= tag.asInstanceOf[String]
            }
            filterSet.selectedTags = selectedTags
          }
        }
      })
      contents = listView
    }, new Constraints {grid=(0,1)})
  }
  
  add(new FlowPanel {
    hGap = 10
    contents += tagsPanel
    contents += new UnmutePane(tableModel, filterSet)
  }, new Constraints {grid=(0,0); gridwidth=3; anchor=Anchor.West})
  
  val excludeNotToYouReplies = new CheckBox("Exclude replies not to you")
  add(excludeNotToYouReplies, new Constraints {grid=(0,2); gridwidth=3; anchor=Anchor.West})
  
  class MatchField extends TextField {columns=20; minimumSize=new Dimension(100, preferredSize.height)}

  add(new Label("Include"), new Constraints {grid=(0,3); ipadx=5})
  val includeMatching = 
    new MatchField {tooltip="Include only tweets that match this string or regular expression"}
  add(includeMatching, new Constraints {grid=(1,3); ipadx=5})
  
  class RegexCheckBox extends CheckBox("Regex") {
    tooltip = "Whether this search argument is a regular expression"
  }
  
  val includeIsRegex = new RegexCheckBox
  add(includeIsRegex, new Constraints {grid=(2,3); anchor=Anchor.West})

  add(new Label("Exclude"), new Constraints {grid=(0,4); ipadx=5})
  val excludeMatching = new MatchField {tooltip="Exclude tweets that match this string or regular expression"}
  add(excludeMatching, new Constraints {grid=(1,4); ipadx=5})

  val excludeIsRegex = new RegexCheckBox
  add(excludeIsRegex, new Constraints {grid=(2,4); anchor=Anchor.West})

  add(new Label(""), new Constraints {grid=(10,0); fill=Fill.Horizontal; weightx=1})
  add(new Label(""), new Constraints {grid=(0,10); fill=Fill.Vertical; weighty=1})

  def applyChanges {
    filterSet.excludeNotToYouReplies = excludeNotToYouReplies.selected
    filterSet.includeTextFilters = if (includeMatching.text.length > 0) List(new TextFilter(includeMatching.text, includeIsRegex.selected)) else List[TextFilter]()
    filterSet.excludeTextFilters = if (excludeMatching.text.length > 0) List(new TextFilter(excludeMatching.text, excludeIsRegex.selected)) else List[TextFilter]()
    filterSet.publish
  }
}

 