package org.talkingpuffin.ui

import _root_.scala.swing.GridBagPanel._
import filter.{TextFilters, TagUsers, FilterSet}
import java.awt.event.KeyEvent
import java.awt.{Dimension}
import javax.swing.border.EmptyBorder
import swing.{Orientation, BoxPanel, TabbedPane, Frame, GridBagPanel, FlowPanel, Button, CheckBox, Action}

/**
 * Dialog for setting filters
 */
class FiltersDialog(paneTitle: String, tableModel: StatusTableModel, filterSet: FilterSet, 
    tagUsers: TagUsers) extends Frame {
  title = paneTitle + " Filters"
  preferredSize = new Dimension(600, 600)
  val generalPane = new GridBagPanel {
    border = new EmptyBorder(5, 5, 0, 5)
    add(new UnmutePane("Muted users", tableModel, filterSet, filterSet.mutedUsers, tableModel.unmuteUsers),
      new Constraints {grid=(1,0); anchor=Anchor.West; fill=Fill.Vertical; weighty=1})
    add(new UnmutePane("Retweet-Muted users", tableModel, filterSet, filterSet.retweetMutedUsers, 
      tableModel.unmuteRetweetUsers),
      new Constraints {grid=(2,0); anchor=Anchor.West; fill=Fill.Vertical; weighty=1})
    val excludeFriendRetweets = new CheckBox("Exclude retweets of statuses of people you follow")
    add(excludeFriendRetweets, new Constraints {grid=(0,1); gridwidth=3; anchor=Anchor.West})
    val excludeNonFollowers = new CheckBox("Exclude non-followers")
    add(excludeNonFollowers, new Constraints {grid=(0,2); gridwidth=3; anchor=Anchor.West})
  }
  val includePane = new InOutPane("Only Tweets Containing One of", filterSet.includeSet.textFilters)
  val excludePane = new InOutPane("Tweets Containing Any of", filterSet.excludeSet.textFilters)
  val tabbedPane = new TabbedPane {
    pages += new TabbedPane.Page("General", generalPane)
    pages += new TabbedPane.Page("Include", includePane)
    pages += new TabbedPane.Page("Exclude", excludePane)
  }
  contents = new BoxPanel(Orientation.Vertical) {
    contents += tabbedPane
    contents += new FlowPanel {
      val applyAction = Action("Apply") {applyChanges}
      contents += new Button(applyAction)
      val okAction = new Action("OK") {
        mnemonic = KeyEvent.VK_O
        def apply = {applyChanges; FiltersDialog.this.visible = false}
      }
      val okButton = new Button(okAction) 
      defaultButton = okButton
      contents += okButton
    }
  }
  peer.setLocationRelativeTo(null)
  
  def addIncludeMatching(text: String) {
    includePane.table.addTextFilter(text, false)
    applyChanges
  }

  def addExcludeMatching(text: String) {
    excludePane.table.addTextFilter(text, false)
    applyChanges
  }

  def applyChanges {
    filterSet.includeSet.tags = includePane.tagsPanel.selectedTags
    filterSet.excludeSet.tags = excludePane.tagsPanel.selectedTags
    filterSet.excludeFriendRetweets = generalPane.excludeFriendRetweets.selected
    filterSet.excludeNonFollowers = generalPane.excludeNonFollowers.selected
    filterSet.publish
  }

  class InOutPane(textPrompt: String, textFilters: TextFilters) extends GridBagPanel {
    val tagsPanel = new TagsPanel(true, false, tagUsers, List[String]()) {
      minimumSize = new Dimension(180, 100)
    }
  
    add(tagsPanel, new Constraints {grid=(0,0); anchor=Anchor.West; fill=Fill.Vertical; weighty=1})

    val table = new TextFilterControl(textPrompt, textFilters)
    add(table, new Constraints {
      grid=(0,3); gridwidth=3; anchor=Anchor.West; fill=Fill.Both; weightx=1; weighty=1
    })
    
  }
}


