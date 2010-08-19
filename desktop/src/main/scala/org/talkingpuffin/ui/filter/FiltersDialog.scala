package org.talkingpuffin.ui.filter

import java.awt.event.KeyEvent
import org.talkingpuffin.filter.{CompoundFilter, TextTextFilter, TagUsers, FilterSet}
import org.talkingpuffin.util.Loggable
import org.talkingpuffin.ui.{StatusTableModel}
import swing.{TabbedPane, Orientation, BoxPanel, Frame, FlowPanel, Action, Button}
import java.awt.Dimension

/**
 * Dialog for setting filters
 */
class FiltersDialog(paneTitle: String, tableModel: StatusTableModel, filterSet: FilterSet, 
    tagUsers: TagUsers) extends Frame with Loggable {
  
  title = paneTitle + " Filters"
  preferredSize = new Dimension(600, 600)
  
  val generalPane = new GeneralPane
  val includePane = new IncludeExcludePane(tagUsers, "Only Tweets Matching One of", 
    filterSet.includeSet.cpdFilters)
  val excludePane = new IncludeExcludePane(tagUsers, "Tweets Matching Any of", 
    filterSet.excludeSet.cpdFilters)
  
  contents = new BoxPanel(Orientation.Vertical) {
    contents += new TabbedPane {
      pages += new TabbedPane.Page("Include", includePane)
      pages += new TabbedPane.Page("Exclude", excludePane)
      pages += new TabbedPane.Page("General", generalPane)
    }
    contents += new FlowPanel {
      val applyAction = Action("Apply") {applyChanges}
      contents += new Button(applyAction)
      val okAction = Action("OK") {applyChanges; FiltersDialog.this.visible = false}
      val okButton = new Button(okAction) 
      defaultButton = okButton
      contents += okButton
    }
  }
  peer.setLocationRelativeTo(null)
  
  def addIncludeMatching(text: String) {
    includePane.table.addFilter(CompoundFilter(List(TextTextFilter(text, false)), None, None))
    applyChanges
  }

  def addExcludeMatching(text: String) {
    excludePane.table.addFilter(CompoundFilter(List(TextTextFilter(text, false)), None, None))
    applyChanges
  }

  def applyChanges {
    filterSet.includeSet.tags       = includePane.tagsPanel            .selectedTags
    filterSet.excludeSet.tags       = excludePane.tagsPanel            .selectedTags
    filterSet.excludeFriendRetweets = generalPane.excludeFriendRetweets.selected
    filterSet.excludeNonFollowers   = generalPane.excludeNonFollowers  .selected
    filterSet.useNoiseFilters       = generalPane.useNoiseFilters      .selected
    
    filterSet.publish
  }
}
