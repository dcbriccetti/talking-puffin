package org.talkingpuffin.ui

import _root_.scala.swing.GridBagPanel._
import _root_.scala.swing.{ListView, Frame, GridBagPanel, UIElement, FlowPanel, Button, CheckBox, Label, ScrollPane, Action}
import filter.{TagsRepository, FilterSet, TextFilter, FilterSetChanged}
import java.awt.event.KeyEvent
import java.awt.{Dimension, Insets}
import javax.swing.border.EmptyBorder
import javax.swing.event.{ListSelectionListener, ListSelectionEvent, TableModelListener, TableModelEvent}
import javax.swing.{JTable, BorderFactory}

/**
 * Dialog for setting filters
 */

class FiltersDialog(paneTitle: String, tableModel: StatusTableModel, filterSet: FilterSet) extends Frame {
  title = (paneTitle + " Filters")
  val panel = new GridBagPanel {
    border = new EmptyBorder(5, 5, 0, 5)
    val tagsPanel = new TagsPanel(true) {
      minimumSize = new Dimension(180, 100)
    }
  
    add(tagsPanel, new Constraints {grid=(0,0); anchor=Anchor.West; fill=Fill.Vertical; weighty=1})
    add(new UnmutePane(tableModel, filterSet),
      new Constraints {grid=(1,0); anchor=Anchor.West; fill=Fill.Vertical; weighty=1})
    add(new Label(" "), new Constraints {grid=(2,0); fill=Fill.Horizontal; weightx=1})
  
    val excludeNotToFollowingReplies = new CheckBox("No replies not to following")
    val excludeOverlapping = new CheckBox("No Tweets appearing in other sessions")
    add(new FlowPanel(FlowPanel.Alignment.Left) {
      contents += excludeNotToFollowingReplies
      contents += excludeOverlapping
    }, new Constraints {grid=(0,2); gridwidth=3; anchor=Anchor.West})
  
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
      val okAction = new Action("OK") {
        mnemonic = KeyEvent.VK_O
        def apply = {applyChanges; FiltersDialog.this.visible = false}
      }
      val okButton = new Button(okAction) 
      defaultButton = okButton
      contents += okButton
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
    filterSet.selectedTags = panel.tagsPanel.selectedTags
    filterSet.excludeNotToFollowingReplies = panel.excludeNotToFollowingReplies.selected
    filterSet.excludeOverlapping = panel.excludeOverlapping.selected
    filterSet.publish
  }
}
