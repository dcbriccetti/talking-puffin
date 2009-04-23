package com.davebsoft.sctw.ui

import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.{ListView, Frame, GridBagPanel, UIElement, FlowPanel, Button, CheckBox, Label, ScrollPane, Action}
import filter.{TagsRepository, FilterSet, TextFilter, FilterSetChanged}
import java.awt.{Dimension, Insets}
import javax.swing.event.{ListSelectionListener, ListSelectionEvent, TableModelListener, TableModelEvent}
import javax.swing.{JTable, BorderFactory}

/**
 * Dialog for setting filters
 * @author Dave Briccetti
 */

class FiltersDialog(paneTitle: String, tableModel: StatusTableModel, filterSet: FilterSet) extends Frame {
  title = (paneTitle + " Filters")
  val panel = new GridBagPanel {
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
  
    val includeTable = new TextFilterControl("Include Only Tweets Containing One of", filterSet.includeTextFilters)
    add(includeTable, new TextFilterTableConstraints(3))
    val excludeTable = new TextFilterControl("Exclude Tweets Containing Any of", filterSet.excludeTextFilters)
    add(excludeTable, new TextFilterTableConstraints(5))
  
    add(new FlowPanel {
      val applyAction = Action("Apply") {applyChanges}
      contents += new Button(applyAction)
      val okAction = Action("OK") {applyChanges; FiltersDialog.this.visible = false}
      contents += new Button(okAction)
      val cancelAction = Action("Cancel") {FiltersDialog.this.visible = false}
      contents += new Button(cancelAction)
    }, new Constraints {grid=(0,6); fill=Fill.Horizontal; weightx=1})
    
    preferredSize = new Dimension(550, 600)

  }
  contents = panel
  peer.setLocationRelativeTo(null)
  
  def addIncludeMatching(text: String) {
    panel.includeTable.addTextFilter(text, false)
    applyChanges
  }

  def addExcludeMatching(text: String) {
    panel.excludeTable.addTextFilter(text, false)
    applyChanges
  }

  def applyChanges {
    filterSet.excludeNotToYouReplies = panel.excludeNotToYouReplies.selected
    filterSet.publish
  }
}
