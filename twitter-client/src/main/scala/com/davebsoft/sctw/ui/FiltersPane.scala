package com.davebsoft.sctw.ui

import _root_.scala.swing._
import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.event.ButtonClicked
import filter.{TagsRepository, FilterSet, TextFilter, FilterSetChanged}
import java.awt.{Dimension, Insets}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent, TableModelListener, TableModelEvent}
import javax.swing.{JTable, BorderFactory}

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
  
  class TextFilterTableConstraints(y: Int) extends Constraints {
    grid=(0,y); gridwidth=3; anchor=Anchor.West; fill=Fill.Both; weightx=1
  }
  
  val includeTable = new TextFilterControl("Include Only Tweets Containing", filterSet.includeTextFilters)
  add(includeTable, new TextFilterTableConstraints(3))
  val excludeTable = new TextFilterControl("Exclude Tweets Containing", filterSet.excludeTextFilters)
  add(excludeTable, new TextFilterTableConstraints(5))
  
  add(new Label(""), new Constraints {grid=(10,0); fill=Fill.Horizontal; weightx=1})
  add(new Label(""), new Constraints {grid=(0,10); fill=Fill.Vertical; weighty=1})

  def addIncludeMatching(text: String) {
    filterSet.includeTextFilters.add(new TextFilter(text, false))
    includeTable.dataChanged
    applyChanges
  }

  def addExcludeMatching(text: String) {
    filterSet.excludeTextFilters.add(new TextFilter(text, false))  
    excludeTable.dataChanged
    applyChanges
  }

  def applyChanges {
    filterSet.excludeNotToYouReplies = excludeNotToYouReplies.selected
    filterSet.publish
  }
}

 